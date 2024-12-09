package fr.labomg.biophonie.core.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.labomg.biophonie.core.data.DefaultPreferencesRepository
import fr.labomg.biophonie.core.data.PreferencesRepository

@Module
@InstallIn(SingletonComponent::class)
abstract class PreferencesDataModule {

    @Binds
    internal abstract fun bindPreferencesRepository(
        impl: DefaultPreferencesRepository
    ): PreferencesRepository
}
