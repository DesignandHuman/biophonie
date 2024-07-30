package fr.labomg.biophonie.data.user.source

import fr.labomg.biophonie.data.user.source.remote.Message
import fr.labomg.biophonie.data.user.source.remote.User

interface UserDataSource {
    suspend fun pingRestricted(): Result<Message>

    suspend fun getUser(user: User? = null): Result<User>

    suspend fun saveUser(user: User): Result<User>

    fun isUserConnected(): Boolean
}
