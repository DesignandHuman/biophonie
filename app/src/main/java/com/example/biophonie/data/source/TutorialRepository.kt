package com.example.biophonie.data.source

import com.example.biophonie.data.source.remote.WebService
import com.example.biophonie.network.NetworkAddUser
import com.example.biophonie.network.NetworkUser
import com.example.biophonie.util.AppPrefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TutorialRepository(
    private val webService: WebService
) {

    suspend fun postUser(name: String): Result<NetworkUser> {
        return withContext(Dispatchers.IO){
            webService.postUser(NetworkAddUser(name))
                .onSuccess { storeUser(it) }
        }
    }

    private fun storeUser(user: NetworkUser){
        AppPrefs.userId = user.userId
        AppPrefs.userName = user.name
        AppPrefs.password = user.password
    }
}