package fr.labomg.biophonie.feature.firstlaunch

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.findNavController
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.AndroidEntryPoint
import fr.labomg.biophonie.core.ui.theme.AppTheme
import fr.labomg.biophonie.core.work.ClearCacheWorker
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TutorialFragment : Fragment() {

    private val viewModel: TutorialViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view =
            ComposeView(requireContext()).apply {
                setViewCompositionStrategy(
                    ViewCompositionStrategy.DisposeOnLifecycleDestroyed(viewLifecycleOwner)
                )
                setUpViewModelCollector()
                setContent()
            }
        setUpClearCacheWorker()
        return view
    }

    private fun ComposeView.setUpViewModelCollector() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.shouldStartMapExploration.collect { shouldStart ->
                    startMapExploration(shouldStart)
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    private fun ComposeView.setContent() {
        setContent {
            val windowSizeClass = calculateWindowSizeClass(requireActivity())
            AppTheme {
                Surface(color = MaterialTheme.colorScheme.primary) {
                    TutorialScreen(windowWidth = windowSizeClass.widthSizeClass)
                }
            }
        }
    }

    private fun ComposeView.startMapExploration(shouldStart: Boolean) {
        if (shouldStart) {
            findNavController()
                .navigate("android-app://fr.labomg.biophonie/exploregeopoints".toUri())
        }
    }

    private fun setUpClearCacheWorker() {
        val requestBuilder =
            PeriodicWorkRequestBuilder<ClearCacheWorker>(
                DAYS_BETWEEN_WORK_REQUEST,
                TimeUnit.DAYS,
                1,
                TimeUnit.DAYS
            )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val now = LocalDateTime.now()
            val tonight = LocalDateTime.of(now.toLocalDate(), LocalTime.MIDNIGHT)
            requestBuilder.setInitialDelay(
                Duration.between(now, tonight.plusDays(DAYS_BETWEEN_WORK_REQUEST))
            )
        }
        WorkManager.getInstance(requireContext())
            .enqueueUniquePeriodicWork(
                ClearCacheWorker.WORK_NAME,
                ExistingPeriodicWorkPolicy.REPLACE,
                requestBuilder.build()
            )
    }

    companion object {
        private const val DAYS_BETWEEN_WORK_REQUEST = 14L
    }
}
