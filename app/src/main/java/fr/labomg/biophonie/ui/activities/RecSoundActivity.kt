package fr.labomg.biophonie.ui.activities

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import fr.labomg.biophonie.R
import fr.labomg.biophonie.viewmodels.RecViewModel

@AndroidEntryPoint
class RecSoundActivity : AppCompatActivity() {

    private val viewModel: RecViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rec_sound)
        viewModel.setCoordinates(intent.extras)
    }
}
