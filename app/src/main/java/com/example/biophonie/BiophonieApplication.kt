package com.example.biophonie

import android.app.Application
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import androidx.appcompat.app.AppCompatDelegate
import com.example.biophonie.data.source.GeoPointRepository
import com.example.biophonie.data.source.TutorialRepository
import com.example.biophonie.ui.activities.TutorialActivity
import com.example.biophonie.util.AppPrefs
import com.example.biophonie.util.MyDebugTree
import com.example.biophonie.util.ReleaseTree
import timber.log.Timber
import timber.log.Timber.Forest.plant


class BiophonieApplication: Application() {
    val geoPointRepository: GeoPointRepository
        get() = ServiceLocator.provideGeoPointRepository(this)

    val tutorialRepository: TutorialRepository
        get() = ServiceLocator.provideTutorialRepository()

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG)
            plant(MyDebugTree())
        else
            plant(ReleaseTree())
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        AppPrefs.setup(this)
        checkTutorial()
    }

    private fun checkTutorial() {
        if (AppPrefs.userId == null) {
            val intent =
                Intent(this, TutorialActivity::class.java). apply {
                    addFlags(FLAG_ACTIVITY_NEW_TASK)
                }
            startActivity(intent)
        }
    }
}