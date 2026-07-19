package com.degree.homedash.shared.api

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.js.Js

actual fun createHttpClient(config: HttpClientConfig<*>.() -> Unit): HttpClient =
    HttpClient(Js, config)
