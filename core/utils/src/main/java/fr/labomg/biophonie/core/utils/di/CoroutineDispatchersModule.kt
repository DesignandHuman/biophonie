package fr.labomg.biophonie.core.utils.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

@Retention(AnnotationRetention.RUNTIME) @Qualifier annotation class DefaultDispatcher

@Retention(AnnotationRetention.RUNTIME) @Qualifier annotation class IoDispatcher

@Retention(AnnotationRetention.RUNTIME) @Qualifier annotation class MainDispatcher

@Retention(AnnotationRetention.BINARY) @Qualifier annotation class MainImmediateDispatcher

@InstallIn(SingletonComponent::class)
@Module
object CoroutinesDispatchersModule {

    @DefaultDispatcher
    @Provides
    fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

    @IoDispatcher @Provides fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @MainDispatcher @Provides fun provideMainDispatcher(): CoroutineDispatcher = Dispatchers.Main

    @MainImmediateDispatcher
    @Provides
    fun provideMainImmediateDispatcher(): CoroutineDispatcher = Dispatchers.Main.immediate
}
