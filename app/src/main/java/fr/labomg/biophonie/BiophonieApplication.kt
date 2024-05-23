package fr.labomg.biophonie

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import fr.labomg.biophonie.util.AppPrefs
import fr.labomg.biophonie.util.MyDebugTree
import fr.labomg.biophonie.util.ReleaseTree
import javax.inject.Inject
import timber.log.Timber.Forest.plant

@HiltAndroidApp
class BiophonieApplication : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    override fun getWorkManagerConfiguration() =
        Configuration.Builder().setWorkerFactory(workerFactory).build()

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) plant(MyDebugTree()) else plant(ReleaseTree())
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        AppPrefs.setup(this)
    }
}
