package fr.labomg.biophonie.core.network.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.labomg.biophonie.core.network.UserService
import javax.inject.Singleton
import retrofit2.Retrofit

@InstallIn(SingletonComponent::class)
@Module
object UserServiceModule {

    @Singleton
    @Provides
    fun provideUserService(@Unauthenticated retrofit: Retrofit): UserService {
        return retrofit.create(UserService::class.java)
    }
}
