package fr.labomg.biophonie.util

import android.util.Log
import fr.labomg.biophonie.BuildConfig
import timber.log.Timber

class MyDebugTree: Timber.DebugTree() {
    override fun createStackElementTag(element: StackTraceElement): String? {
        return "(${element.fileName}:${element.lineNumber})#${element.methodName}"
    }
}

class ReleaseTree: Timber.DebugTree() {

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        // Crashlytics ?
        if (priority == Log.ERROR)
            Timber.tag(BuildConfig.APPLICATION_ID).wtf(t, message)
    }
}