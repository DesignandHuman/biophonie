package com.example.biophonie.data.source

import com.example.biophonie.data.source.remote.ClientWeb
import com.example.biophonie.network.AccessToken
import com.example.biophonie.network.NetworkAuthUser
import com.example.biophonie.util.AppPrefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TokenRepository {
    suspend fun fetchAccessToken(): Result<AccessToken> {
        val user = buildAuthUser()
        return if (user != null) {
            withContext(Dispatchers.IO) {
                ClientWeb.webService.refreshToken(user)
                    .onSuccess { AppPrefs.token = it.token }
            }
        } else {
            Result.failure(RuntimeException("user not in shared pref"))
        }
    }

    private fun buildAuthUser(): NetworkAuthUser? {
        val username = AppPrefs.userName
        val password = AppPrefs.password
        return if (username != null && password != null)
            NetworkAuthUser(username,password)
        else null
    }
}