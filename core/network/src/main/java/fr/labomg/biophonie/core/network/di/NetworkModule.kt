package fr.labomg.biophonie.core.network.di

import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.labomg.biophonie.core.network.AccessTokenAuthenticator
import fr.labomg.biophonie.core.network.AuthenticationInterceptor
import fr.labomg.biophonie.core.network.BuildConfig
import fr.labomg.biophonie.core.network.HttpLoggingInterceptorLevel
import fr.labomg.biophonie.core.network.ResultCallAdapterFactory
import javax.inject.Qualifier
import javax.inject.Singleton
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

@InstallIn(SingletonComponent::class)
@Module
object NetworkModule {

    @Singleton
    @Provides
    @Unauthenticated
    fun provideOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(
                HttpLoggingInterceptor().apply { level = HttpLoggingInterceptorLevel },
            )
            .build()

    @Singleton
    @Provides
    @Unauthenticated
    fun provideRetrofit(
        jsonConverter: MoshiConverterFactory,
        @Unauthenticated okHttpClient: OkHttpClient,
        @BaseUrl baseUrl: String,
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(jsonConverter)
            .addCallAdapterFactory(ResultCallAdapterFactory())
            .client(okHttpClient)
            .build()

    @Singleton
    @Provides
    @Authenticated
    fun provideAuthenticatedOkHttpClient(
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
    fun provideAuthenticatedRetrofit(
        jsonConverter: MoshiConverterFactory,
        @Authenticated okHttpClient: OkHttpClient,
        @BaseUrl baseUrl: String,
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(jsonConverter)
            .addCallAdapterFactory(ResultCallAdapterFactory())
            .client(okHttpClient)
            .build()

    @Singleton
    @Provides
    fun provideJsonConverterFactory(): MoshiConverterFactory {
        val moshi = Moshi.Builder().build()
        return MoshiConverterFactory.create(moshi).asLenient()
    }

    @Qualifier @Retention(AnnotationRetention.BINARY) annotation class BaseUrl

    @Singleton @Provides @BaseUrl fun provideBaseUrl(): String = BuildConfig.BASE_URL
}
