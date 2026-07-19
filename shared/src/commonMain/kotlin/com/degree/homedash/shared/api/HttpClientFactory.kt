package com.degree.homedash.shared.api

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig

/** Each platform supplies an HttpClient backed by a target-appropriate engine. */
expect fun createHttpClient(config: HttpClientConfig<*>.() -> Unit): HttpClient
