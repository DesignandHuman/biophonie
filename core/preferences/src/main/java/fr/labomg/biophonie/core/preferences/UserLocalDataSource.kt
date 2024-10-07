package fr.labomg.biophonie.core.preferences

import fr.labomg.biophonie.core.preferences.model.PrefUser
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserLocalDataSource
@Inject
constructor(
    @Suppress("UNUSED_PARAMETER") appPrefs: AppPrefs,
) {

    fun getUser(): Result<PrefUser> {
        return Result.success(
            PrefUser(
                id = AppPrefs.userId,
                name = AppPrefs.userName,
                password = AppPrefs.password,
                token = AppPrefs.token
            )
        )
    }

    fun saveUser(user: PrefUser): Result<PrefUser> {
        AppPrefs.userId = user.id
        AppPrefs.userName = user.name
        AppPrefs.password = user.password
        AppPrefs.token = user.token
        return Result.success(user)
    }

    fun isUserConnected(): Boolean = (AppPrefs.userName != "")
}
