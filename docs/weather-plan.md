# Weather Feature — NWS (api.weather.gov) Direct Integration

> Status: **planned, not started.** Saved 2026-07-11 for later. This is a design/implementation plan,
> not shipped code.

## Context

Side-project feature: show **current outdoor temp / dew point / conditions** and surface **active
severe-weather alerts**, sourced directly from the US National Weather Service API
(`api.weather.gov`). NWS is free, needs no API key, and is the single source that covers both asks. It
requires a `User-Agent` header and is US-only.

This is the app's **first HTTP (non-WebSocket) usage**, but the foundations already exist: Ktor 3.4.0
with a `createHttpClient(config)` expect/actual seam (OkHttp on Android, Js on wasmJs) at
`shared/.../network/HttpClientFactory.kt`, and `kotlinx-serialization-json` wired in commonMain.

**Location:** read from Home Assistant — the app already streams all entity states, and HA always
publishes `zone.home` with `latitude`/`longitude` attributes. So no new persistence, settings field,
or WebSocket protocol work: `repo.entity("zone.home")?.attrDouble("latitude")` /`("longitude")`
(`HomeAssistantRepo.entity`, `EntityState.attrDouble`).

**Defaults chosen** (adjust before building): display in °F; gate behind a new Rob-only
`FeatureFlag.ViewWeather` (consistent with the other WIP sections); conditions shown on a dedicated
**Weather** screen (temp + dew-point climate cards + a conditions header); alerts surfaced as Home
`WarningCard`s. Wind/humidity/pressure are easy follow-ups, out of scope for v1.

## Reused patterns (with paths)

- Networking seam: `shared/.../network/HttpClientFactory.kt` (`createHttpClient { }`); lenient `Json`
  config style from `shared/.../network/HaProtocol.kt`.
- Repo construction: manual in `AppViewModel` (constructor default), then passed down through `App.kt`
  — mirror `repository = HomeAssistantRepo(HaWebSocketClient())`.
- Screen/card scaffold: copy the Pets screen (`pets/PetsScreen.kt`) + `Screen.Pets` nav branch + Home
  `DashboardCard` in `HomeScreen.kt`.
- Climate cards: `EntityUi.Climate` + `EntityMetadata.Climate`/`ClimateKind` + `ClimateCard`, laid out
  via `ControlGroup(entities, useCardUis = true, onAction)`. Projection modeled on
  `livingroom/LivingRoomViewModel.kt` (`toClimate`, `dewPointClimate`). Synthetic entity ids
  (e.g. `"weather.outdoor_temp"`) are fine — read-only climate cards only use the id as a list key.
- Home warnings: `HomeViewModel.warnings` (`HomeWarning`, `WarningSeverity`) → auto-rendered by
  `WarningCard` in `HomeScreen.kt`. `WarningSeverity.Critical` = red, `Warning` = amber.
- Flag gating: `FeatureFlag` + `FeatureFlagDao` (`setOf("Rob")`), surfaced via `AppViewModel.featureFlags`,
  gated in UI with `if (showWeather)` exactly like `showOffice`.

## Build change

In `shared/build.gradle.kts` commonMain add (both already in the version catalog, unused today):
`implementation(libs.ktor.client.content.negotiation)` and `implementation(libs.ktor.serialization.json)`
— enables `install(ContentNegotiation) { json(lenientJson) }` + typed `client.get(url).body<Dto>()`.
(Alternative with zero build change: `client.get(url).bodyAsText()` + `Json.decodeFromString`, matching
HaProtocol's style. Either is fine; ContentNegotiation is cleaner across the multiple endpoints.)

## Phase 1 — Weather data layer (`shared`)

New `shared/.../data/WeatherRepo.kt` + `shared/.../network/NwsDto.kt` (+ domain models):
- `createHttpClient { install(ContentNegotiation){ json(lenientJson) }; install(DefaultRequest){ header(HttpHeaders.UserAgent, "homedashboard-kmm (robert.degree@gmail.com)") } }`.
- Domain models: `WeatherConditions(tempC: Double?, dewpointC: Double?, humidity: Double?, description: String?, location: String?)`, `WeatherAlert(event: String, severity: AlertSeverity, headline: String?)`, `enum class AlertSeverity { Extreme, Severe, Moderate, Minor, Unknown }`.
- `suspend fun conditions(lat, lon): WeatherConditions` — chain, coordinates rounded to 4 decimals:
  1. `GET /points/{lat},{lon}` → `properties.observationStations` (URL) + `properties.relativeLocation.properties.city/state`.
  2. `GET {observationStations}` → first `features[].properties.stationIdentifier`.
  3. `GET /stations/{id}/observations/latest` → `properties.temperature.value` (°C), `.dewpoint.value` (°C), `.relativeHumidity.value` (%), `.textDescription`.
  Cache the points→station lookup in-memory keyed by rounded `lat,lon` (it's static per location).
- `suspend fun alerts(lat, lon): List<WeatherAlert>` — `GET /alerts/active?point={lat},{lon}` → map `features[].properties.{event, severity, headline}`.
- DTOs `@Serializable` with `@SerialName`; NWS numeric fields are nullable objects (`{value, unitCode}`) — model as nullable.

Wire into `AppViewModel`: add `val weatherRepository: WeatherRepo = WeatherRepo(createHttpClient { ... })` (constructor default, like `repository`).

## Phase 2 — Weather screen (conditions)

New package `weather/`:
- `WeatherViewModel(haRepo: HomeAssistantRepo, weatherRepo: WeatherRepo)` — derive lat/lon from
  `haRepo.states["zone.home"]`; on first available (and on manual refresh) `viewModelScope.launch { runCatching { weatherRepo.conditions(lat, lon) } }`. Expose
  `WeatherUiState(location: String?, description: String?, cards: List<EntityUi.Climate>, loading, error)`.
  Build cards: Outdoor Temp (`ClimateKind.Temperature`) and Dew Point (`ClimateKind.DewPoint`, humidity as `subvalueText`), converting °C→°F for display (reuse `formatNumber`). Empty/`"—"` state when no `zone.home` or fetch fails.
- `WeatherScreen.kt` — copy `PetsScreen.kt`: `DashboardScaffold(title = "Weather", …)`, a conditions
  header (`ControlGroup("Now") { Text(description + location) }`), then `ControlGroup(entities = ui.cards, useCardUis = true, onAction = {})`. Stateless `WeatherContent` + `@Preview` with sample data.
- `App.kt`: add `Weather` to the `Screen` enum; add `Screen.Weather -> WeatherScreen(repository, weatherRepository, onBack = ::goBack, onOpenSettings = { navigate(Screen.Settings) })`; pass `onOpenWeather = { navigate(Screen.Weather) }` to `HomeScreen`.
- `HomeScreen.kt`: add `onOpenWeather` + `showWeather: Boolean` params; render `if (showWeather) DashboardCard("Weather", Icons.Filled.Cloud, AppColors.Accent, onOpenWeather)`.
- Flag: add `ViewWeather` to `FeatureFlag`; `FeatureFlag.ViewWeather to setOf("Rob")` in `FeatureFlagDao`; pass `showWeather = FeatureFlag.ViewWeather in featureFlags` from `App.kt`.

## Phase 3 — Severe-weather alerts on Home

- `AppViewModel`: own `weatherAlerts: StateFlow<List<WeatherAlert>>`. In `init`, launch a coroutine that
  reads lat/lon from `repository.states` (`zone.home`) and polls `weatherRepository.alerts(lat, lon)`
  every ~10 min (re-fetch on lat/lon change), `runCatching` around each poll. Default empty.
- `HomeViewModel(repo, weatherAlerts: StateFlow<List<WeatherAlert>> = MutableStateFlow(emptyList()))` —
  `combine(repo.states, weatherAlerts)`; append a `HomeWarning` per alert. Map severity:
  `Extreme/Severe → WarningSeverity.Critical` (red), else `Warning` (amber). Message = `headline ?: event`.
- `App.kt` / `HomeScreen.kt`: thread `appVm.weatherAlerts` into `HomeScreen` → `viewModel { HomeViewModel(repository, weatherAlerts) }`. Rendering is automatic (`WarningCard`).

## Verification

- Build: `./gradlew :composeApp:compileDebugKotlinAndroid` (and `:shared` compiles for wasmJs).
- Unit-test the pure bits in `commonTest`: coordinate formatting (4-decimal `"lat,lon"`) and °C→°F
  conversion; optionally an NWS JSON DTO decode against a captured sample payload.
- Live check without a real HA/`zone.home`: temporarily hardcode a known lat/lon (e.g. a US city) in
  `WeatherViewModel` to exercise the NWS chain, confirm temp/dew-point cards populate and (if any active)
  an alert appears; then revert to `zone.home`.
- Runtime (as Rob): Home shows the gated **Weather** card → Weather screen shows conditions + temp/dew
  point; any active NWS alert appears as a red/amber `WarningCard` on Home.
- Web target sanity: NWS supports CORS, so the wasmJs build can call it — verify a fetch succeeds in the
  browser build (not just Android).
