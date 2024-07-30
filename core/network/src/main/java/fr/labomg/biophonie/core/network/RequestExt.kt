package fr.labomg.biophonie.core.network

import okhttp3.Request

fun Request.addAuthorizationHeader(token: String): Request {
    return newBuilder().header("Authorization", token).build()
}
