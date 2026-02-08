package net.sevoos.rest

import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.cio.CIO

actual fun getEngineFactory(): HttpClientEngineFactory<HttpClientEngineConfig> = CIO