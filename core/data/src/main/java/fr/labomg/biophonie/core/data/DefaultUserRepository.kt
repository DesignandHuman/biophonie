package fr.labomg.biophonie.core.data

import fr.labomg.biophonie.core.model.User
import fr.labomg.biophonie.core.network.UserRemoteDataSource
import fr.labomg.biophonie.core.network.model.toExternal
import fr.labomg.biophonie.core.network.model.toNetwork
import fr.labomg.biophonie.core.preferences.UserLocalDataSource
import fr.labomg.biophonie.core.preferences.model.toExternal
import fr.labomg.biophonie.core.preferences.model.toPref
import fr.labomg.biophonie.core.utils.di.IoDispatcher
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class DefaultUserRepository
@Inject
constructor(
    private val userRemoteDataSource: UserRemoteDataSource,
    private val userLocalDataSource: UserLocalDataSource,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : UserRepository {
    override suspend fun getToken(): Result<String> =
        withContext(ioDispatcher) {
            if (!userLocalDataSource.isUserConnected()) {
                return@withContext Result.failure(Exception("User not connected"))
            }
            return@withContext userLocalDataSource.getUser().map { it.token }
        }

    override suspend fun addUser(name: String): Result<User> =
        withContext(ioDispatcher) {
            return@withContext userRemoteDataSource
                .saveUser(name)
                .onSuccess { userLocalDataSource.saveUser(it.toExternal().toPref()) }
                .map { it.toExternal() }
        }

    override suspend fun refreshToken(): Result<String> {
        return with(userLocalDataSource.getUser()) {
            if (isSuccess) {
                val prefUser = this.getOrThrow()
                userRemoteDataSource.getToken(prefUser.toExternal().toNetwork()).onSuccess {
                    userLocalDataSource.saveUser(prefUser.copy(token = it))
                }
            } else {
                this.map { "" }
            }
        }
    }

    override fun isUserConnected(): Boolean = userLocalDataSource.isUserConnected()
}
