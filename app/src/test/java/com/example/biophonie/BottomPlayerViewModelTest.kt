package com.example.biophonie

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragment
import androidx.test.core.app.ApplicationProvider
import com.example.biophonie.repositories.GeoPointRepository
import com.example.biophonie.ui.fragments.BottomPlayerFragment
import com.example.biophonie.viewmodels.BottomPlayerViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
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