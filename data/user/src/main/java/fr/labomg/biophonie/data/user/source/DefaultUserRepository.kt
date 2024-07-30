package fr.labomg.biophonie.data.user.source

import fr.labomg.biophonie.data.user.di.LocalDataSource
import fr.labomg.biophonie.data.user.di.RemoteDataSource
import fr.labomg.biophonie.data.user.source.remote.User
import javax.inject.Inject

class DefaultUserRepository
@Inject
constructor(
    @RemoteDataSource private val userRemoteDataSource: UserDataSource,
    @LocalDataSource private val userLocalDataSource: UserDataSource
) : UserRepository {
    override suspend fun getUser(): Result<User> {
        return userLocalDataSource.getUser()
    }

    override suspend fun addUser(name: String): Result<User> {
        val user = User(name = name)
        return userRemoteDataSource.saveUser(user).onSuccess { userLocalDataSource.saveUser(it) }
    }

    override suspend fun refreshAccessToken(): Result<String> {
        return with(userLocalDataSource.getUser()) {
            if (isSuccess) {
                userRemoteDataSource
                    .getUser(this.getOrNull())
                    .onSuccess { userLocalDataSource.saveUser(it) }
                    .map { it.token ?: "" }
            } else {
                this.map { "" }
            }
        }
    }

    override fun isUserConnected(): Boolean = userLocalDataSource.isUserConnected()
}
