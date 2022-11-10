package com.example.biophonie.repositories

import com.example.biophonie.network.*
import com.example.biophonie.util.AppPrefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TutorialRepository {
    suspend fun postUser(name: String): Result<NetworkUser> {
        return withContext(Dispatchers.IO){
            ClientWeb.webService.postUser(NetworkAddUser(name))
                .onSuccess { storeUser(it) }
        }
    }

    private fun storeUser(user: NetworkUser){
        AppPrefs.userId = user.userId
        AppPrefs.userName = user.name
        AppPrefs.password = user.password
    }
}