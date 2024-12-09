@file:OptIn(MapboxExperimental::class)

package fr.labomg.biophonie.feature.exploregeopoints

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.os.Build
import android.view.View
import android.view.WindowInsets
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import com.dropbox.dropshots.Dropshots
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.maps.MapboxExperimental
import com.mapbox.maps.dsl.cameraOptions
import fr.labomg.biophonie.core.model.Coordinates
import fr.labomg.biophonie.core.model.GeoPoint
import fr.labomg.biophonie.core.testing.EmptyTestActivity
import fr.labomg.biophonie.core.testing.util.readAsset
import fr.labomg.biophonie.core.ui.theme.AppTheme
import fr.labomg.biophonie.feature.exploregeopoints.Constants.FRANCE_LATITUDE
import fr.labomg.biophonie.feature.exploregeopoints.Constants.FRANCE_LONGITUDE
import fr.labomg.biophonie.feature.exploregeopoints.Constants.INITIAL_ZOOM_LEVEL
import java.time.Instant
import javax.net.ssl.HttpsURLConnection
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MapTest {

    @get:Rule val composeRule = createAndroidComposeRule<EmptyTestActivity>()

    @get:Rule val dropshots = Dropshots()

    @get:Rule val permissionRule = GrantPermissionRule.grant(ACCESS_COARSE_LOCATION)

    private lateinit var mockWebServer: MockWebServer
    private val mockGeoJsonResponse =
        InstrumentationRegistry.getInstrumentation().readAsset("testgeojson.json").trimIndent()
    private lateinit var mockGeoJsonUrl: String
    private val cameraOptions = cameraOptions {
        center(Point.fromLngLat(FRANCE_LONGITUDE, FRANCE_LATITUDE))
        zoom(INITIAL_ZOOM_LEVEL)
    }

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        mockWebServer.enqueue(
            MockResponse().setBody(mockGeoJsonResponse).setResponseCode(HttpsURLConnection.HTTP_OK)
        )
        mockGeoJsonUrl = mockWebServer.url("/geojson.json").toString()
        composeRule.hideSystemBars()
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun displayMap_withSelectedPoint() {
        val selectedPoint = mutableStateOf<Feature?>(null)

        composeRule.setContent {
            AppTheme {
                Map(
                    cameraOptions = cameraOptions,
                    sourceUrl = mockGeoJsonUrl,
                    selectedPoint = selectedPoint.value
                )
            }
        }
        waitAndAssertSnapshot("MapWithNoSelection")
        selectedPoint.value = FeatureCollection.fromJson(mockGeoJsonResponse).features()?.get(0)
        waitAndAssertSnapshot("MapWithFirstPointSelected", true)
        selectedPoint.value = FeatureCollection.fromJson(mockGeoJsonResponse).features()?.get(1)
        waitAndAssertSnapshot("MapWithSecondPointSelected", true)
        selectedPoint.value = null
        waitAndAssertSnapshot("MapWithUnselected", true)
    }

    @Test
    fun displayMap_withUnavailablePoints() {
        val selectedPoint = mutableStateOf<Feature?>(null)
        val unavailableGeoPoints =
            listOf<GeoPoint>(
                GeoPoint(
                    id = 3,
                    remoteId = 0,
                    coordinates = Coordinates(2.5220067, 46.2885133),
                    title = "Third Point",
                    date = Instant.now(),
                    amplitudes = listOf(),
                    picture = "",
                    sound = ""
                ),
                GeoPoint(
                    id = 4,
                    remoteId = 4,
                    coordinates = Coordinates(3.5220067, 47.2885133),
                    title = "Fourth Point synced",
                    date = Instant.now(),
                    amplitudes = listOf(),
                    picture = "",
                    sound = ""
                )
            )

        composeRule.setContent {
            AppTheme {
                Map(
                    cameraOptions = cameraOptions,
                    unavailableGeoPoints = unavailableGeoPoints,
                    sourceUrl = mockGeoJsonUrl,
                    selectedPoint = selectedPoint.value
                )
            }
        }
        waitAndAssertSnapshot("MapWithUnavailableGeoPoints")
        selectedPoint.value = unavailableGeoPoints[0].toMapboxFeature()
        waitAndAssertSnapshot("MapWithUnavailableSelected", true)
        selectedPoint.value = unavailableGeoPoints[1].toMapboxFeature()
        waitAndAssertSnapshot("MapWithUnavailableSyncedSelected", true)
    }

    @Test
    fun displayMap_trackLocation() {
        val operationState = mutableStateOf<OperationState>(OperationState.WaitingToTrack)
        composeRule.setContent {
            AppTheme {
                Map(
                    cameraOptions = cameraOptions,
                    sourceUrl = mockGeoJsonUrl,
                    operationState = operationState.value,
                    onTrackingStart = { operationState.value = OperationState.Tracking }
                )
            }
        }
        composeRule.waitUntil(5000) { operationState.value == OperationState.Tracking }
    }

    private fun waitAndAssertSnapshot(name: String, triggerRecomposition: Boolean = false) {
        if (triggerRecomposition) {
            composeRule.onRoot().assertExists()
        }
        composeRule.waitForIdle()
        Thread.sleep(1000)
        dropshots.assertSnapshot(name)
    }
}

private fun AndroidComposeTestRule<ActivityScenarioRule<EmptyTestActivity>, EmptyTestActivity>
    .hideSystemBars() {
    this.activityRule.scenario.onActivity { activity ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            activity.window.insetsController?.hide(WindowInsets.Type.systemBars())
        } else {
            @Suppress("DEPRECATION")
            activity.window.decorView.systemUiVisibility =
                (View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        }
    }
}
