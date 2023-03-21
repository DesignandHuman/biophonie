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
}