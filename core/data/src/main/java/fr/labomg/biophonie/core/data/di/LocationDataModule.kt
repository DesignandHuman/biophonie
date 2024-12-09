package fr.labomg.biophonie.core.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.labomg.biophonie.core.data.DefaultLocationRepository
import fr.labomg.biophonie.core.data.LocationRepository

@Module
@InstallIn(SingletonComponent::class)
abstract class LocationDataModule {

    @Binds
    internal abstract fun bindLocationRepository(
        impl: DefaultLocationRepository
    ): LocationRepository
}
