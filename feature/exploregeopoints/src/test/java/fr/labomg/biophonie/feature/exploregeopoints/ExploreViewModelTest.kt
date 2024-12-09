@file:OptIn(ExperimentalCoroutinesApi::class)

package fr.labomg.biophonie.feature.exploregeopoints

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraState
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.dsl.cameraOptions
import fr.labomg.biophonie.core.testing.data.geoPointTestData
import fr.labomg.biophonie.core.testing.repository.TestGeoPointRepository
import fr.labomg.biophonie.core.testing.repository.TestLocationRepository
import fr.labomg.biophonie.core.testing.repository.TestPreferencesRepository
import fr.labomg.biophonie.core.testing.repository.TestUserRepository
import fr.labomg.biophonie.core.testing.util.MainDispatcherRule
import fr.labomg.biophonie.feature.exploregeopoints.ExploreViewModel.Companion.RECORDING_PERMISSIONS
import fr.labomg.biophonie.feature.exploregeopoints.ExploreViewModel.Companion.REQUIRED_RECORDING_PERMISSIONS
import fr.labomg.biophonie.feature.exploregeopoints.ExploreViewModel.Companion.REQUIRED_TRACKING_PERMISSIONS
import fr.labomg.biophonie.feature.exploregeopoints.ExploreViewModel.Companion.TRACKING_PERMISSIONS
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock

class ExploreViewModelTest {

    @get:Rule val dispatcherRule = MainDispatcherRule()

    // used to test LiveData
    @get:Rule val instantTaskExecutorRule = InstantTaskExecutorRule()

    val mockContext = mock(Application::class.java)

    private lateinit var viewModel: ExploreViewModel

    @Before
    fun setUp() {
        viewModel =
            ExploreViewModel(
                locationRepository = TestLocationRepository(),
                geoPointRepository = TestGeoPointRepository(),
                userRepository = TestUserRepository(),
                preferencesRepository = TestPreferencesRepository(),
                appContext = mockContext
            )
    }

    @Test
    fun uiState_initialization_isInitial() = runTest {
        with(viewModel.uiState.value) {
            assertEquals(OperationState.Idle, operationState)
            assertFalse(shouldShowAboutDialog)
            assertFalse(shouldRequestGps)
            assertNull(selectedPoint)
        }
    }

    @Test
    fun oneGeoPoint_whenSelected_isDisplayed() = runTest {
        val geoPointToSelect = geoPointTestData[1]
        collectUiStateInBackground()
        viewModel.onPointClick(geoPointToSelect.id)
        assertEquals(geoPointToSelect.toMapboxFeature(), viewModel.uiState.value.selectedPoint)
    }

    @Test
    fun viewModel_onTrack_shouldRequestTrackingPermissions() = runTest {
        collectUiStateInBackground()

        viewModel.onTrackClick()
        assertEquals(TRACKING_PERMISSIONS, viewModel.uiState.value.requestPermissions)
    }

    @Test
    fun viewModel_onRecord_shouldRequestRecordingPermissions() = runTest {
        collectUiStateInBackground()

        viewModel.onRecordClick()
        assertEquals(RECORDING_PERMISSIONS, viewModel.uiState.value.requestPermissions)
    }

    @Test
    fun viewModel_onRecordAndNoPermissionGranted_shouldBecomeIdle() = runTest {
        collectUiStateInBackground()
        viewModel.onRecordClick()
        val noPermissionGranted = RECORDING_PERMISSIONS.associateBy({ it }, { false })
        viewModel.handlePermissionsResult(noPermissionGranted)
        with(viewModel.uiState.value) {
            assertEquals(emptyList<String>(), requestPermissions)
            assertEquals(OperationState.Idle, operationState)
            assertEquals(REQUIRED_RECORDING_PERMISSIONS, missingPermissions)
        }
    }

    @Test
    fun viewModel_onTrackAndNoPermissionGranted_shouldBecomeIdle() = runTest {
        collectUiStateInBackground()
        viewModel.onTrackClick()
        val noPermissionGranted = TRACKING_PERMISSIONS.associateBy({ it }, { false })
        viewModel.handlePermissionsResult(noPermissionGranted)
        with(viewModel.uiState.value) {
            assertEquals(emptyList<String>(), requestPermissions)
            assertEquals(OperationState.Idle, operationState)
            assertEquals(REQUIRED_TRACKING_PERMISSIONS, missingPermissions)
        }
    }

    @Test
    fun viewModel_onTrackAndCoarseLocationGranted_shouldWaitForTracking() = runTest {
        collectUiStateInBackground()
        viewModel.onTrackClick()
        val coarseLocationGranted =
            mapOf(
                android.Manifest.permission.ACCESS_COARSE_LOCATION to true,
                android.Manifest.permission.ACCESS_FINE_LOCATION to false
            )
        viewModel.handlePermissionsResult(coarseLocationGranted)
        with(viewModel.uiState.value) {
            assertEquals(emptyList<String>(), requestPermissions)
            assertEquals(OperationState.WaitingToTrack, operationState)
            assertEquals(emptyList<String>(), missingPermissions)
        }
    }

    @Test
    fun viewModel_onRecordAndRequiredPermissionGranted_shouldRecord() = runTest {
        collectUiStateInBackground()
        viewModel.onRecordClick()
        val recordAndCoarseLocationGranted =
            mapOf(
                android.Manifest.permission.RECORD_AUDIO to true,
                android.Manifest.permission.ACCESS_COARSE_LOCATION to true,
                android.Manifest.permission.ACCESS_FINE_LOCATION to false
            )
        viewModel.handlePermissionsResult(recordAndCoarseLocationGranted)
        with(viewModel.uiState.value) {
            assertEquals(emptyList<String>(), requestPermissions)
            assertEquals(OperationState.Recording, operationState)
            assertEquals(emptyList<String>(), missingPermissions)
        }
    }

    // no need to cancel collection when test ends because backgroundScope is used
    private fun TestScope.collectUiStateInBackground() {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.uiState.collect() }
    }

    @Test
    fun viewModel_onClose_shouldSaveCameraConfiguration() = runTest {
        val longitude = 0.0
        val latitude = 1.0
        val zoom = 13.0
        val cameraState =
            CameraState(
                Point.fromLngLat(longitude, latitude),
                EdgeInsets(0.0, 0.0, 0.0, 0.0),
                zoom,
                0.0,
                0.0
            )
        viewModel.saveCameraState(cameraState)
        val expectedCameraOptions = cameraOptions {
            center(Point.fromLngLat(longitude, latitude))
            zoom(zoom)
        }
        assertEquals(expectedCameraOptions, viewModel.cameraOptions.first())
    }
}
