package fr.labomg.biophonie.data.user.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.labomg.biophonie.data.user.source.UserDataSource
import fr.labomg.biophonie.data.user.source.local.UserLocalDataSource
import fr.labomg.biophonie.data.user.source.remote.UserRemoteDataSource
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier annotation class LocalDataSource

@Qualifier annotation class RemoteDataSource

@InstallIn(SingletonComponent::class)
@Module
abstract class UserLocalModule {

    @Singleton
    @Binds
    @LocalDataSource
    abstract fun bindUserLocalDataSource(impl: UserLocalDataSource): UserDataSource
}

@InstallIn(SingletonComponent::class)
@Module
abstract class UserRemoteModule {

    @Singleton
    @Binds
    @RemoteDataSource
    abstract fun bindUserRemoteDataSource(impl: UserRemoteDataSource): UserDataSource
}
