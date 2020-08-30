package com.example.biophonie.work

import android.content.Context
import android.util.Log
import androidx.work.*
import com.example.biophonie.BuildConfig
import com.example.biophonie.database.NewSoundDatabase
import com.example.biophonie.repositories.GeoJsonRepository
import retrofit2.HttpException

private const val TAG = "SyncSoundsWorker"

class SyncSoundsWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        Log.d(TAG, "doWork: ")
        val database = NewSoundDatabase.getInstance(applicationContext)
        val repository = GeoJsonRepository(database)
        var finalResult = true
        try {
            val newSounds = database.soundDao.getNewSounds()
            for (newSound in newSounds){
                val success = repository.sendNewSound(newSound)
                Log.d(TAG, "doWork: sound ${newSound.title} success? $success")
                if (!success) finalResult = false && continue
                else if (!BuildConfig.DEBUG) repository.deleteNewSound(newSound)
            }
        } catch (e: HttpException) {
            Log.d(TAG, "doWork: failure")
            return Result.failure()
        }
        return if(finalResult) Result.success() else Result.failure()
    }

    companion object {
        const val WORK_NAME = "com.example.biophonie.mapviewmodel.SyncSoundsWorker"
    }
}