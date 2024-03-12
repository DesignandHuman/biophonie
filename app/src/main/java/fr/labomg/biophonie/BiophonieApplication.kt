package fr.labomg.biophonie

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import fr.labomg.biophonie.data.source.GeoPointRepository
import fr.labomg.biophonie.data.source.TutorialRepository
import fr.labomg.biophonie.util.AppPrefs
import fr.labomg.biophonie.util.MyDebugTree
import fr.labomg.biophonie.util.ReleaseTree
import timber.log.Timber.Forest.plant

class BiophonieApplication : Application() {
    val geoPointRepository: GeoPointRepository
        get() = ServiceLocator.provideGeoPointRepository(this)

    val tutorialRepository: TutorialRepository
        get() = ServiceLocator.provideTutorialRepository()

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) plant(MyDebugTree()) else plant(ReleaseTree())
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        AppPrefs.setup(this)
    }
}
