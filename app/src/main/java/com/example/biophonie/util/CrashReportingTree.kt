package com.example.biophonie.util

import android.util.Log
import timber.log.Timber

class ReleaseTree: Timber.DebugTree() {

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        // Crashlytics ?
        if (priority == Log.ERROR)
            e(t, message, tag)
    }
}