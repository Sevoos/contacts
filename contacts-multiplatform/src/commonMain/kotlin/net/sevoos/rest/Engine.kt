package net.sevoos.rest

import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.HttpClientEngineFactory

expect fun getEngineFactory(): HttpClientEngineFactory<HttpClientEngineConfig>