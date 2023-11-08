package fr.labomg.biophonie

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.room.Room
import fr.labomg.biophonie.data.source.DefaultGeoPointRepository
import fr.labomg.biophonie.data.source.GeoPointDataSource
import fr.labomg.biophonie.data.source.GeoPointRepository
import fr.labomg.biophonie.data.source.TutorialRepository
import fr.labomg.biophonie.data.source.local.GeoPointDatabase
import fr.labomg.biophonie.data.source.local.GeoPointLocalDataSource
import fr.labomg.biophonie.data.source.remote.GeoPointRemoteDataSource
import fr.labomg.biophonie.data.source.remote.WebClient
import kotlinx.coroutines.runBlocking

object ServiceLocator {
    private val lock = Any()

    private var webClient: WebClient? = null
    private var database: GeoPointDatabase? = null
    @Volatile
    private var geoPointRepository: GeoPointRepository? = null
    private var tutorialRepository: TutorialRepository? = null

    fun provideGeoPointRepository(context: Context): GeoPointRepository {
        synchronized(this) {
            return geoPointRepository ?: createGeoPointRepository(context)
        }
    }

    fun provideTutorialRepository(): TutorialRepository {
        synchronized(this) {
            return tutorialRepository ?: createTutorialRepository()
        }
    }

    private fun createGeoPointRepository(context: Context): GeoPointRepository {
        val newRepo = DefaultGeoPointRepository(
            createGeoPointRemoteDataSource(),
            createGeoPointLocalDataSource(context)
        )
        geoPointRepository = newRepo
        return newRepo
    }

    private fun createTutorialRepository(): TutorialRepository {
        val newRepo = TutorialRepository(
            (webClient ?: createWebClient()).webService
        )
        tutorialRepository = newRepo
        return newRepo
    }

    private fun createGeoPointLocalDataSource(context: Context): GeoPointDataSource {
        val database = database ?: createDataBase(context)
        return GeoPointLocalDataSource(database.geoPointDao())
    }

    private fun createGeoPointRemoteDataSource(): GeoPointDataSource {
        val webClient = webClient ?: createWebClient()
        return GeoPointRemoteDataSource(webClient.webService)
    }

    private fun createWebClient(): WebClient {
        val client = WebClient()
        webClient = client
        return client
    }

    private fun createDataBase(context: Context): GeoPointDatabase {
        val result = Room.databaseBuilder(
            context.applicationContext,
            GeoPointDatabase::class.java,
            "new_geopoint_database"
        )
            .fallbackToDestructiveMigration()
            .build()
        database = result
        return result
    }

    @VisibleForTesting
    fun resetGeoPointRepository() {
        synchronized(lock) {
            runBlocking {
                //GeoPointRemoteDataSource.deleteAllGeoPoints()
            }
            database?.apply {
                clearAllTables()
                close()
            }
            database = null
            geoPointRepository = null
        }
    }
}