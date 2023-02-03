package com.example.biophonie

import android.app.Application
import com.example.biophonie.data.source.GeoPointRepository
import com.example.biophonie.data.source.TutorialRepository

class BiophonieApplication: Application() {
    val geoPointRepository: GeoPointRepository
        get() = ServiceLocator.provideGeoPointRepository(this)

    val tutorialRepository: TutorialRepository
        get() = ServiceLocator.provideTutorialRepository()
}