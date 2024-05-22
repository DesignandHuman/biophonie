package fr.labomg.biophonie.data.source.remote

import okhttp3.Request

fun Request.addAuthorizationHeader(token: String): Request {
    return newBuilder().header("Authorization", token).build()
}
