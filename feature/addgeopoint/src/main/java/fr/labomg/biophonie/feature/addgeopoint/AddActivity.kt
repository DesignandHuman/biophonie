package fr.labomg.biophonie.feature.addgeopoint

import android.os.Bundle
import androidx.activity.viewModels
import androidx.fragment.app.FragmentActivity
import androidx.navigation.navArgs
import dagger.hilt.android.AndroidEntryPoint
import fr.labomg.biophonie.core.model.Coordinates

@AndroidEntryPoint
class AddActivity : FragmentActivity() {

    private val viewModel: AddViewModel by viewModels()
    private val safeArgs: AddActivityArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add)
        viewModel.setCoordinates(Coordinates(safeArgs.longitude, safeArgs.latitude))
    }
}
