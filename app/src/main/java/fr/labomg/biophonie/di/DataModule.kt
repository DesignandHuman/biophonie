package fr.labomg.biophonie.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.labomg.biophonie.data.source.DefaultGeoPointRepository
import fr.labomg.biophonie.data.source.GeoPointRepository

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    internal abstract fun bindGeoPointRepository(impl: DefaultGeoPointRepository): GeoPointRepository

}

