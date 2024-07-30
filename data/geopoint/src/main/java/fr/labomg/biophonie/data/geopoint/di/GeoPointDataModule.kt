package fr.labomg.biophonie.data.geopoint.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.labomg.biophonie.data.geopoint.source.DefaultGeoPointRepository
import fr.labomg.biophonie.data.geopoint.source.GeoPointRepository

@Module
@InstallIn(SingletonComponent::class)
abstract class GeoPointDataModule {

    @Binds
    internal abstract fun bindGeoPointRepository(
        impl: DefaultGeoPointRepository
    ): GeoPointRepository
}
