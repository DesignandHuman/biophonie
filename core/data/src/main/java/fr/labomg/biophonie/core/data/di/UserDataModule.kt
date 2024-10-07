package fr.labomg.biophonie.core.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.labomg.biophonie.core.data.DefaultUserRepository
import fr.labomg.biophonie.core.data.UserRepository
import fr.labomg.biophonie.core.utils.TokenProvider

@Module
@InstallIn(SingletonComponent::class)
abstract class UserDataModule {

    @Binds internal abstract fun bindUserRepository(impl: DefaultUserRepository): UserRepository

    @Binds internal abstract fun bindTokenProvider(impl: UserRepository): TokenProvider
}
