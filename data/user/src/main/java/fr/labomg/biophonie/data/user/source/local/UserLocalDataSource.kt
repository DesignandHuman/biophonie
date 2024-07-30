package fr.labomg.biophonie.data.user.source.local

import fr.labomg.biophonie.data.user.source.UserDataSource
import fr.labomg.biophonie.data.user.source.remote.Message
import fr.labomg.biophonie.data.user.source.remote.User
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserLocalDataSource
@Inject
constructor(
    @Suppress("UNUSED_PARAMETER") appPrefs: AppPrefs,
) : UserDataSource {
    override suspend fun pingRestricted(): Result<Message> {
        // NO-OP
        return Result.success(Message(""))
    }

    override suspend fun getUser(user: User?): Result<User> {
        return Result.success(
            User(
                id = AppPrefs.userId ?: 0,
                name = AppPrefs.userName!!,
                password = AppPrefs.password,
                token = AppPrefs.token
            )
        )
    }

    override suspend fun saveUser(user: User): Result<User> {
        AppPrefs.userId = user.id
        AppPrefs.userName = user.name
        AppPrefs.password = user.password
        AppPrefs.token = user.token
        return Result.success(user)
    }

    override fun isUserConnected(): Boolean = (AppPrefs.userName == null)
}
