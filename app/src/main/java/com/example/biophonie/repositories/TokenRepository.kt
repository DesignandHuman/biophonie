package com.example.biophonie.repositories

import com.example.biophonie.network.AccessToken
import com.example.biophonie.network.ClientWeb
import com.example.biophonie.network.NetworkAuthUser
import com.example.biophonie.util.AppPrefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.Exception

class TokenRepository {
    suspend fun fetchAccessToken(): AccessToken? {
        val user = buildAuthUser()
        if (user != null) {
            return withContext(Dispatchers.IO) {
                val response = ClientWeb.webService.refreshToken(user)
                if (response.isSuccessful && response.body() != null) {
                    AppPrefs.token = response.body()!!.token
                    return@withContext response.body()
                }
                else throw Exception("NetworkError") //TODO
            }
        } else {
            return null
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