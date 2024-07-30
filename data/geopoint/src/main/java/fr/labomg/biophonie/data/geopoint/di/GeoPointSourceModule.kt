package fr.labomg.biophonie.data.geopoint.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.labomg.biophonie.core.network.di.LocalDataSource
import fr.labomg.biophonie.core.network.di.RemoteDataSource
import fr.labomg.biophonie.data.geopoint.source.GeoPointDataSource
import fr.labomg.biophonie.data.geopoint.source.local.GeoPointLocalDataSource
import fr.labomg.biophonie.data.geopoint.source.remote.GeoPointRemoteDataSource
import javax.inject.Singleton

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
