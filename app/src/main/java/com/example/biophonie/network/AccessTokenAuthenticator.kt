package com.example.biophonie.network

import com.example.biophonie.repositories.TokenRepository
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class AccessTokenAuthenticator: Authenticator {

    private val repository = TokenRepository()
    override fun authenticate(route: Route?, response: Response): Request? {
        return if (response.retryCount > 2) null
        else response.createSignedRequest()
    }

    private fun Response.createSignedRequest(): Request? {
        var newToken: AccessToken?
        runBlocking {
            newToken = repository.fetchAccessToken()
        }
        return if (newToken != null) {
            request.signWithToken(newToken!!.token)
        } else null
    }
}

fun Request.signWithToken(token: String) =
    newBuilder()
        .header("Authorization", token)
        .build()

private val Response.retryCount: Int
    get() {
        var currentResponse = priorResponse
        var result = 0
        while (currentResponse != null) {
            result++
            currentResponse = currentResponse.priorResponse
        }
        return result
    }
