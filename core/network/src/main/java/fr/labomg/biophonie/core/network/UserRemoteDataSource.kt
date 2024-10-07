package fr.labomg.biophonie.core.network

import fr.labomg.biophonie.core.network.model.Message
import fr.labomg.biophonie.core.network.model.NetworkAddUser
import fr.labomg.biophonie.core.network.model.NetworkAuthUser
import fr.labomg.biophonie.core.network.model.NetworkUser
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRemoteDataSource @Inject constructor(private val userService: UserService) {
    suspend fun pingRestricted(): Result<Message> {
        return userService.pingRestricted()
    }

    suspend fun getToken(user: NetworkUser): Result<String> {
        val authUser = NetworkAuthUser(user.name, user.password)
        return userService.refreshToken(authUser).map { it.token }
    }

    suspend fun saveUser(name: String): Result<NetworkUser> {
        return userService.postUser(NetworkAddUser(name))
    }
}
