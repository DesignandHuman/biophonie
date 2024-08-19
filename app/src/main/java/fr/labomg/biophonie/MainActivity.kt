package fr.labomg.biophonie

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.AndroidEntryPoint
import fr.labomg.biophonie.core.work.SyncSoundsWorker

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {
        super.onResume()
        syncNewSounds()
    }

    private fun syncNewSounds() {
        val constraints =
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()
        val workRequest =
            OneTimeWorkRequestBuilder<SyncSoundsWorker>().setConstraints(constraints).build()
        WorkManager.getInstance(this)
            .enqueueUniqueWork(
                SyncSoundsWorker.WORK_NAME,
                ExistingWorkPolicy.APPEND_OR_REPLACE,
                workRequest
            )
    }
}
