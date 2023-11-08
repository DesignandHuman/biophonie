package fr.labomg.biophonie

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragment
import fr.labomg.biophonie.ui.fragments.BottomPlayerFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
import org.junit.Rule
import org.junit.Test

class BottomPlayerFragmentTest {
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Test
    fun changingViewModelId_ShouldExpandFragment() {
        val scenario = launchFragment<BottomPlayerFragment>()
        scenario.onFragment { fragment ->
            fragment.viewModel.geoPointId.value = 1
            assert(fragment.bottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED)
        }
    }
}