package fr.labomg.biophonie.data.user.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.labomg.biophonie.core.network.HttpLoggingInterceptorLevel
import fr.labomg.biophonie.core.network.ResultCallAdapterFactory
import fr.labomg.biophonie.core.network.di.Authenticated
import fr.labomg.biophonie.core.network.di.NetworkModule
import fr.labomg.biophonie.core.network.di.Unauthenticated
import fr.labomg.biophonie.data.user.source.remote.AccessTokenAuthenticator
import fr.labomg.biophonie.data.user.source.remote.AuthenticationInterceptor
import fr.labomg.biophonie.data.user.source.remote.UserService
import javax.inject.Singleton
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

@InstallIn(SingletonComponent::class)
@Module
object RemoteModule {

    @Singleton
    @Provides
    @Authenticated
    fun provideOkHttpClient(
        authenticationInterceptor: AuthenticationInterceptor,
        accessTokenAuthenticator: AccessTokenAuthenticator,
    ): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(
                HttpLoggingInterceptor().apply { level = HttpLoggingInterceptorLevel },
            )
            .addInterceptor(authenticationInterceptor)
            .authenticator(accessTokenAuthenticator)
            .build()

    @Singleton
    @Provides
    @Authenticated
    fun provideRetrofit(
        jsonConverter: MoshiConverterFactory,
        @Authenticated okHttpClient: OkHttpClient,
        @NetworkModule.BaseUrl baseUrl: String,
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(jsonConverter)
            .addCallAdapterFactory(ResultCallAdapterFactory())
            .client(okHttpClient)
            .build()

    @Singleton
    @Provides
    fun provideUserService(@Unauthenticated retrofit: Retrofit): UserService {
        return retrofit.create(UserService::class.java)
    }
}
