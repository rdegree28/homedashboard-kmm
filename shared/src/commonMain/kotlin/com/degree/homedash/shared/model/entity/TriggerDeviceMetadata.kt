package com.degree.homedash.shared.model.entity

/**
 * A pressable card that fires a Home Assistant service — activating a scene, running a script,
 * triggering an automation.
 *
 * Unlike the other metadata types this one has no live state to show: [entityId] is synthetic (there
 * is no `trigger.*` domain in Home Assistant), and exists only to give the card a stable list key.
 *
 * The metadata *describes* the call rather than making it: [action] returns a [ServiceCall] and the
 * ViewModel executes it through `HomeAssistantRepo`, keeping this package free of any dependency on
 * the transport layer. Build the common ones with the factories in TriggerEntityMetadataFactory.kt.
 */
abstract class TriggerDeviceMetadata(
    override val entityId: String,
    override val displayName: String,
) : DeviceMetadata {

    /** The service call this card fires when pressed. */
    abstract fun action(): ServiceCall

    /**
     * A Home Assistant service invocation: `domain.service` aimed at [entityId].
     *
     * Deliberately not a Ktor/JSON type — it's a plain description the repo turns into a call.
     */
    data class ServiceCall(
        val domain: String,
        val service: String,
        val entityId: String,
    ) {
        companion object {
            /**
             * `turn_on` for [entityId], with the domain read off the id.
             *
             * This is how scenes, scripts and automations are all activated: `scene.turn_on`,
             * `script.turn_on`, `automation.turn_on`.
             */
            fun turnOn(entityId: String): ServiceCall =
                ServiceCall(domain = entityId.substringBefore('.'), service = "turn_on", entityId = entityId)
        }
    }

    companion object
}
