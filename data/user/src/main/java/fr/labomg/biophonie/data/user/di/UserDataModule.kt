package fr.labomg.biophonie.data.user.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.labomg.biophonie.data.user.source.DefaultUserRepository
import fr.labomg.biophonie.data.user.source.UserRepository

@Module
@InstallIn(SingletonComponent::class)
abstract class UserDataModule {

    @Binds internal abstract fun bindUserRepository(impl: DefaultUserRepository): UserRepository
}
