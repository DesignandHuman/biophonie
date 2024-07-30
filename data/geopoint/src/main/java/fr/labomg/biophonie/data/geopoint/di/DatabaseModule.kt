package fr.labomg.biophonie.data.geopoint.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import fr.labomg.biophonie.data.geopoint.source.local.GeoPointDao
import fr.labomg.biophonie.data.geopoint.source.local.GeoPointDatabase
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext applicationContext: Context): GeoPointDatabase {
        return Room.databaseBuilder(
                applicationContext,
                GeoPointDatabase::class.java,
                "geopoint_database"
            )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideDao(database: GeoPointDatabase): GeoPointDao {
        return database.geoPointDao()
    }
}
