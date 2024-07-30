package fr.labomg.biophonie.data.user.source.remote

import fr.labomg.biophonie.core.network.UnexpectedThrowable
import fr.labomg.biophonie.core.network.di.IoDispatcher
import fr.labomg.biophonie.data.user.source.UserDataSource
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

@Singleton
class UserRemoteDataSource
@Inject
constructor(
    private val userService: UserService,
    @IoDispatcher private val dispatcher: CoroutineDispatcher
) : UserDataSource {
    override suspend fun pingRestricted(): Result<Message> {
        return userService.pingRestricted()
    }

    override suspend fun getUser(user: User?): Result<User> {
        if (user == null) return Result.failure(UnexpectedThrowable())
        else {
            val authUser = NetworkAuthUser(user.name, user.password!!)
            return userService.refreshToken(authUser).map {
                user.token = it.token
                user
            }
        }
    }

    override suspend fun saveUser(user: User): Result<User> {
        return withContext(dispatcher) { userService.postUser(user.toNetworkAddUser()) }
    }

    override fun isUserConnected(): Boolean {
        // NO-OP
        return false
    }
}
