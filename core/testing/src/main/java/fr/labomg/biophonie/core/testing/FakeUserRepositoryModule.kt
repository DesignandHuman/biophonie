package fr.labomg.biophonie.core.testing

import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import fr.labomg.biophonie.core.data.UserRepository
import fr.labomg.biophonie.core.data.di.UserDataModule
import fr.labomg.biophonie.core.model.User
import fr.labomg.biophonie.core.network.ConflictThrowable
import javax.inject.Singleton

@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [UserDataModule::class])
object FakeUserRepositoryModule {
    @Singleton
    @Provides
    fun provideFakeUserRepository() =
        object : UserRepository {

            val dummyUser =
                User(
                    id = 23,
                    name = "Bob",
                    password = "9b768967-d491-4baa-a812-24ea8a9c274d",
                    token = "token"
                )

            override suspend fun addUser(name: String): Result<User> {
                if (name == dummyUser.name) {
                    return Result.failure(ConflictThrowable("not unique"))
                }
                return Result.success(dummyUser.copy(name = name))
            }

            override fun isUserConnected(): Boolean {
                return true
            }

            override suspend fun getToken(): Result<String> {
                return Result.success(dummyUser.token)
            }

            override suspend fun refreshToken(): Result<String> {
                return Result.success(dummyUser.token)
            }
        }
}
