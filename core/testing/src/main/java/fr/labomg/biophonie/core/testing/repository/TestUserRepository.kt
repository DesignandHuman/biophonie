package fr.labomg.biophonie.core.testing.repository

import fr.labomg.biophonie.core.data.UserRepository
import fr.labomg.biophonie.core.model.User
import fr.labomg.biophonie.core.network.ConflictThrowable
import fr.labomg.biophonie.core.testing.data.userTestData
import javax.inject.Inject

class TestUserRepository @Inject constructor() : UserRepository {
    val user = userTestData

    override suspend fun addUser(name: String): Result<User> {
        if (name == user.name) {
            return Result.failure(ConflictThrowable("not unique"))
        }
        return Result.success(user.copy(name = name))
    }

    override fun isUserConnected(): Boolean {
        return true
    }

    override suspend fun getToken(): Result<String> {
        return Result.success(user.token)
    }

    override suspend fun refreshToken(): Result<String> {
        return Result.success(user.token)
    }
}
