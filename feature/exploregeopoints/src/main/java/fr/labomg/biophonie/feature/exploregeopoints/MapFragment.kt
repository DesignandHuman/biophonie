package fr.labomg.biophonie.feature.exploregeopoints

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.mapbox.common.location.Location
import dagger.hilt.android.AndroidEntryPoint
import fr.labomg.biophonie.core.ui.theme.AppTheme

@AndroidEntryPoint
class MapFragment : Fragment() {

    private val viewModel: ExploreViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (BuildConfig.BUILD_TYPE == "release") checkUserConnected()
        return inflater.inflate(R.layout.bottom_player_container, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val viewModel: ExploreViewModel by activityViewModels()

        view.findViewById<ComposeView>(R.id.composeView).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnLifecycleDestroyed(viewLifecycleOwner)
            )
            setContent {
                AppTheme {
                    MapScreen(
                        viewModel = viewModel,
                        onRecord = { location -> launchRecording(location) }
                    )
                }
            }
        }

        if (childFragmentManager.findFragmentById(R.id.fragmentContainer) == null) {
            childFragmentManager
                .beginTransaction()
                .replace(R.id.fragmentContainer, BottomPlayerFragment())
                .commit()
        }
    }

    private fun checkUserConnected() {
        if (!viewModel.isUserConnected()) {
            findNavController().popBackStack()
            findNavController().navigate("android-app://fr.labomg.biophonie/firstlaunch".toUri())
        }
    }

    private fun launchRecording(location: Location) {
        val uri =
            Uri.Builder()
                .scheme("android-app")
                .authority("fr.labomg.biophonie")
                .appendPath("fragment_recording")
                .appendQueryParameter("longitude", location.longitude.toString())
                .appendQueryParameter("latitude", location.latitude.toString())
                .build()
        findNavController().navigate(uri)
    }
}
