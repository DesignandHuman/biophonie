package fr.labomg.biophonie.di

import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.labomg.biophonie.BuildConfig
import fr.labomg.biophonie.data.source.ResultCallAdapterFactory
import fr.labomg.biophonie.data.source.remote.AccessTokenAuthenticator
import fr.labomg.biophonie.data.source.remote.AuthenticationInterceptor
import fr.labomg.biophonie.data.source.remote.WebService
import fr.labomg.biophonie.util.HttpLoggingInterceptorLevel
import javax.inject.Qualifier
import javax.inject.Singleton
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

@InstallIn(SingletonComponent::class)
@Module
object WebModule {
    @Singleton
    @Provides
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
    fun provideJsonConverterFactory(): MoshiConverterFactory {
        val moshi = Moshi.Builder().build()
        return MoshiConverterFactory.create(moshi).asLenient()
    }

    @Qualifier @Retention(AnnotationRetention.BINARY) annotation class BaseUrl

    @Singleton @Provides @BaseUrl fun provideBaseUrl(): String = BuildConfig.BASE_URL

    @Singleton
    @Provides
    fun provideRetrofit(
        jsonConverter: MoshiConverterFactory,
        okHttpClient: OkHttpClient,
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
    fun provideWebService(retrofit: Retrofit): WebService {
        return retrofit.create(WebService::class.java)
    }
}
