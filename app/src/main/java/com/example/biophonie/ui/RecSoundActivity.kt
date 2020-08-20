package com.example.biophonie.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.core.app.BundleCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.example.biophonie.R
import com.example.biophonie.viewmodels.RecViewModel
import fr.haran.soundwave.controller.DefaultRecorderController
import java.util.ArrayList

private const val TAG = "RecSoundActivity"
class RecSoundActivity : AppCompatActivity() {

    private val viewModel: RecViewModel by viewModels{
        RecViewModel.ViewModelFactory(this.application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rec_sound)
    }

    // Used to pass the correct result to NavigationFragments
    // Going through the mask is needed
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode and 0x0000ffff, resultCode, data)
        supportFragmentManager.primaryNavigationFragment?.childFragmentManager?.fragments?.forEach { fragment ->
            fragment.onActivityResult(requestCode and 0x0000ffff, resultCode, data)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.destroyController()
    }
}