package fr.labomg.biophonie.data.user.source

import fr.labomg.biophonie.data.user.source.remote.User

interface UserRepository {
    suspend fun getUser(): Result<User>

    suspend fun addUser(name: String): Result<User>

    suspend fun refreshAccessToken(): Result<String>

    fun isUserConnected(): Boolean
}
