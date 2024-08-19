package fr.labomg.biophonie

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import fr.labomg.biophonie.data.user.source.UserRepository
import fr.labomg.biophonie.feature.exploregeopoints.ExploreViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class BottomPlayerViewModelTest {

    @get:Rule var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: ExploreViewModel

    @Before
    fun setupViewModel() {
        // TODO get database
        val repository = UserRepository()

        viewModel = ExploreViewModel(ApplicationProvider.getApplicationContext())
    }

    @Test
    fun onRetry_shouldChangeGeoPointId() {
        // TODO
    }

    @Test
    fun onLeftClick_shouldChangeRightClickable() {
        // TODO
    }

    @Test
    fun onRightClick_shouldChangeLeftClickable() {
        // TODO
    }
}
