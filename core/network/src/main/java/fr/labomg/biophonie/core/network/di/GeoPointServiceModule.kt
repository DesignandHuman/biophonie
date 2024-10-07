package fr.labomg.biophonie.core.network.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.labomg.biophonie.core.network.GeoPointService
import javax.inject.Singleton
import retrofit2.Retrofit

@InstallIn(SingletonComponent::class)
@Module
object GeoPointServiceModule {

    @Singleton
    @Provides
    fun provideGeoPointService(@Authenticated retrofit: Retrofit): GeoPointService {
        return retrofit.create(GeoPointService::class.java)
    }
}
