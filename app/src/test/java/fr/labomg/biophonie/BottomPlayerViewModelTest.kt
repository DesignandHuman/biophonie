package fr.labomg.biophonie

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import fr.labomg.biophonie.repositories.GeoPointRepository
import fr.labomg.biophonie.viewmodels.BottomPlayerViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class BottomPlayerViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: BottomPlayerViewModel

    @Before
    fun setupViewModel() {
        // TODO get database
        val repository = GeoPointRepository()

        viewModel = BottomPlayerViewModel(ApplicationProvider.getApplicationContext())
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