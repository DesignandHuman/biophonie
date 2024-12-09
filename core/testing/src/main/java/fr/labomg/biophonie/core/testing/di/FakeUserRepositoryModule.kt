package fr.labomg.biophonie.core.testing.di

import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import fr.labomg.biophonie.core.data.UserRepository
import fr.labomg.biophonie.core.data.di.UserDataModule
import fr.labomg.biophonie.core.testing.repository.TestUserRepository
import javax.inject.Singleton

@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [UserDataModule::class])
object FakeUserRepositoryModule {
    @Singleton @Provides fun provideFakeUserRepository(): UserRepository = TestUserRepository()
}
