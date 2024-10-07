package fr.labomg.biophonie.core.data

import fr.labomg.biophonie.core.model.User
import fr.labomg.biophonie.core.utils.TokenProvider

interface UserRepository : TokenProvider {
    suspend fun addUser(name: String): Result<User>

    fun isUserConnected(): Boolean
}
