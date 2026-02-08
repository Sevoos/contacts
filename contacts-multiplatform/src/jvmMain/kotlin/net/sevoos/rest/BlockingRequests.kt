package net.sevoos.rest

import kotlinx.coroutines.runBlocking

inline fun <reified T> RestUtility.getForObjectBlocking(
    mapping: String,
    vararg uriVariables: Any
): T = runBlocking {
    getForObject(mapping, *uriVariables)
}

inline fun <reified T> RestUtility.postForObjectBlocking(
    mapping: String,
    body: Any,
    vararg uriVariables: Any
): T = runBlocking {
    postForObject(mapping, body, *uriVariables)
}