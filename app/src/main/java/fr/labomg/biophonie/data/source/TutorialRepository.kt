package fr.labomg.biophonie.data.source

import fr.labomg.biophonie.data.source.remote.NetworkAddUser
import fr.labomg.biophonie.data.source.remote.NetworkUser
import fr.labomg.biophonie.data.source.remote.WebService
import fr.labomg.biophonie.util.AppPrefs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TutorialRepository(private val webService: WebService) {

    suspend fun postUser(name: String): Result<NetworkUser> {
        return withContext(Dispatchers.IO) {
            webService.postUser(NetworkAddUser(name)).onSuccess { storeUser(it) }
        }
    }

    private fun storeUser(user: NetworkUser) {
        AppPrefs.userId = user.userId
        AppPrefs.userName = user.name
        AppPrefs.password = user.password
    }
}
