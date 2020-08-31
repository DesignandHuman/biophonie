package com.example.biophonie.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.biophonie.R
import com.example.biophonie.viewmodels.RecViewModel

class RecSoundActivity : AppCompatActivity() {

    private val viewModel: RecViewModel by viewModels{
        RecViewModel.ViewModelFactory(this.application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rec_sound)
        viewModel.setCoordinates(intent.extras)
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