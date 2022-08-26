package com.example.biophonie.ui.activities

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

    override fun onDestroy() {
        super.onDestroy()
        // See if there is a leak on the controller.
        // Had to remove that line because on rotating screen, the controller was recreated
        // So we would need to lock screen orientation if there is indeed a leak.
        //viewModel.destroyController()
    }
}