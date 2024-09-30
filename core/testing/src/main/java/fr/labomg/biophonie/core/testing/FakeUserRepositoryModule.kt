package fr.labomg.biophonie.core.testing

import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import fr.labomg.biophonie.core.network.ConflictThrowable
import fr.labomg.biophonie.data.user.di.UserDataModule
import fr.labomg.biophonie.data.user.source.UserRepository
import fr.labomg.biophonie.data.user.source.remote.User
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
                    admin = false,
                    createdOn = "2022-05-26T11:17:35.079344Z",
                    name = "Bob",
                    password = "9b768967-d491-4baa-a812-24ea8a9c274d",
                    token = "token"
                )

            override suspend fun getUser(): Result<User> {
                return Result.success(dummyUser)
            }

            override suspend fun addUser(name: String): Result<User> {
                if (name == dummyUser.name) {
                    return Result.failure(ConflictThrowable("not unique"))
                }
                return Result.success(dummyUser.copy(name = name))
            }

            override suspend fun refreshAccessToken(): Result<String> {
                return Result.success(dummyUser.token!!)
            }

            override fun isUserConnected(): Boolean {
                return true
            }
        }
}
