package fr.labomg.biophonie.core.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.labomg.biophonie.core.data.DefaultGeoPointRepository
import fr.labomg.biophonie.core.data.GeoPointRepository

@Module
@InstallIn(SingletonComponent::class)
abstract class GeoPointDataModule {

    @Binds
    internal abstract fun bindGeoPointRepository(
        impl: DefaultGeoPointRepository
    ): GeoPointRepository
}
