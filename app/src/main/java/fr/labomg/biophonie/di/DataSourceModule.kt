package fr.labomg.biophonie.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.labomg.biophonie.data.source.GeoPointDataSource
import fr.labomg.biophonie.data.source.local.GeoPointLocalDataSource
import fr.labomg.biophonie.data.source.remote.GeoPointRemoteDataSource
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier annotation class LocalDataSource

@Qualifier annotation class RemoteDataSource

@InstallIn(SingletonComponent::class)
@Module
abstract class GeoPointLocalModule {

    @Singleton
    @Binds
    @LocalDataSource
    abstract fun bindGeoPointLocalDataSource(impl: GeoPointLocalDataSource): GeoPointDataSource
}

@InstallIn(SingletonComponent::class)
@Module
abstract class GeoPointRemoteModule {

    @Singleton
    @Binds
    @RemoteDataSource
    abstract fun bindGeoPointRemoteDataSource(impl: GeoPointRemoteDataSource): GeoPointDataSource
}
