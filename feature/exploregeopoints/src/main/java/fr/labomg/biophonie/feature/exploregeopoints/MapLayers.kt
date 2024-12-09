@file:OptIn(MapboxExperimental::class)

package fr.labomg.biophonie.feature.exploregeopoints

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.mapbox.geojson.Feature
import com.mapbox.geojson.Geometry
import com.mapbox.geojson.Point
import com.mapbox.maps.InteractionContext
import com.mapbox.maps.MapboxExperimental
import com.mapbox.maps.extension.compose.MapboxMapComposable
import com.mapbox.maps.extension.compose.style.BooleanValue
import com.mapbox.maps.extension.compose.style.ColorValue
import com.mapbox.maps.extension.compose.style.DoubleListValue
import com.mapbox.maps.extension.compose.style.DoubleValue
import com.mapbox.maps.extension.compose.style.IdGenerator
import com.mapbox.maps.extension.compose.style.StringListValue
import com.mapbox.maps.extension.compose.style.interactions.FeaturesetFeatureScope
import com.mapbox.maps.extension.compose.style.layers.Filter
import com.mapbox.maps.extension.compose.style.layers.FormattedValue
import com.mapbox.maps.extension.compose.style.layers.ImageValue
import com.mapbox.maps.extension.compose.style.layers.generated.SymbolLayer
import com.mapbox.maps.extension.compose.style.layers.generated.TextAnchorValue
import com.mapbox.maps.extension.compose.style.rememberStyleImage
import com.mapbox.maps.extension.compose.style.sources.GeoJSONData
import com.mapbox.maps.extension.compose.style.sources.SourceState
import com.mapbox.maps.extension.compose.style.sources.generated.GeoJsonSourceState
import com.mapbox.maps.extension.compose.style.sources.generated.rememberGeoJsonSourceState
import com.mapbox.maps.extension.style.expressions.dsl.generated.all
import com.mapbox.maps.extension.style.expressions.generated.Expression.Companion.neq
import com.mapbox.maps.interactions.FeatureState
import com.mapbox.maps.interactions.FeaturesetFeature
import fr.labomg.biophonie.core.model.GeoPoint

private const val REMOTE: String = "remote"
private const val CACHE: String = "cache"
private const val LAYER: String = "layer"
private const val SELECTED: String = "selected"
private const val UNSELECTED_CACHE_ICON_RATIO = 0.6
private const val LABEL_OFFSET_LEFT = 0.8
private const val LABEL_OFFSET_RIGHT = -0.05
private const val SELECTED_CACHE_ICON_RATIO = 0.8
private const val UNSELECTED_REMOTE_ICON_RATIO = 0.5
private const val SELECTED_REMOTE_ICON_RATIO = 0.7
internal const val PROPERTY_CACHE: String = "fromCache"

@MapboxMapComposable
@Composable
internal fun UnavailableLayer(
    unavailableGeoPoints: List<GeoPoint>,
    selectedPoint: Feature?,
    onPointClick: (Int, Geometry) -> Unit
) {
    val unavailableSource =
        remember(unavailableGeoPoints) {
            GeoJsonSourceState().apply {
                data = GeoJSONData(value = unavailableGeoPoints.toFeatureList())
            }
        }
    GeoPointLayer(
        layerId = "$CACHE.$LAYER",
        sourceState = unavailableSource,
        selected = false,
        idToFilterOut = selectedPoint?.properties()?.get(PROPERTY_ID)?.asNumber?.toInt(),
        fromCache = true,
        onClick = onPointClick
    )
}

@MapboxMapComposable
@Composable
internal fun SelectedLayer(selectedPoint: Feature?, onClick: (Int, Geometry) -> Unit) {
    val selectedSource =
        remember(selectedPoint) {
            GeoJsonSourceState().apply {
                if (selectedPoint != null) {
                    data = GeoJSONData(value = selectedPoint)
                }
            }
        }
    selectedPoint?.let {
        GeoPointLayer(
            layerId = "$REMOTE.$LAYER.$SELECTED",
            sourceState = selectedSource,
            selected = true,
            fromCache = it.getBooleanProperty(PROPERTY_CACHE) == true,
            onClick = onClick
        )
    }
}

@MapboxMapComposable
@Composable
internal fun RemoteLayer(
    sourceUrl: String,
    selectedPoint: Feature?,
    onPointClick: (Int, Geometry) -> Unit
) {
    val remoteSource = rememberGeoJsonSourceState { data = GeoJSONData(sourceUrl) }
    GeoPointLayer(
        layerId = "$REMOTE.$LAYER",
        sourceState = remoteSource,
        selected = false,
        idToFilterOut = selectedPoint?.properties()?.get(PROPERTY_ID)?.asNumber?.toInt(),
        fromCache = false,
        onClick = onPointClick
    )
}

internal fun List<GeoPoint>.toFeatureList(): List<Feature> =
    this.map {
        Feature.fromGeometry(Point.fromLngLat(it.coordinates.longitude, it.coordinates.latitude))
            .apply {
                addStringProperty(PROPERTY_NAME, it.title)
                addNumberProperty(PROPERTY_ID, it.id)
                addBooleanProperty(PROPERTY_CACHE, true)
            }
    }

@OptIn(MapboxExperimental::class)
@MapboxMapComposable
@Composable
fun GeoPointLayer(
    sourceState: SourceState,
    onClick: (Int, Geometry) -> Unit,
    idToFilterOut: Int? = null,
    selected: Boolean = false,
    fromCache: Boolean = false,
    layerId: String = remember { IdGenerator.generateRandomLayerId("symbol") },
) {
    val color =
        ColorValue(
            if (fromCache) MaterialTheme.colorScheme.secondary
            else MaterialTheme.colorScheme.primary
        )
    val size =
        when {
            selected && fromCache -> SELECTED_CACHE_ICON_RATIO
            selected && !fromCache -> SELECTED_REMOTE_ICON_RATIO
            !selected && fromCache -> UNSELECTED_CACHE_ICON_RATIO
            else -> UNSELECTED_REMOTE_ICON_RATIO
        }
    val fontFamily = if (selected) "Bold" else "Regular"
    val resourceId = if (fromCache) R.drawable.ic_syncing else R.drawable.ic_marker
    val imageId = remember(fromCache) { IdGenerator.generateRandomLayerId("symbol") }
    val marker = rememberStyleImage(imageId = imageId, resourceId = resourceId)
    val expression =
        if (idToFilterOut != null) {
            neq {
                get(PROPERTY_ID)
                literal(idToFilterOut.toLong())
            }
        } else {
            all {}
        }
    val fontSize =
        (if (selected) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyMedium)
            .fontSize
            .value
            .toDouble()

    SymbolLayer(sourceState = sourceState, layerId = layerId) {
        iconAllowOverlap = BooleanValue(true)
        iconColor = color
        iconImage = ImageValue(marker)
        iconSize = DoubleValue(size)
        interactionsState.onClicked(onClick = onGeoPointClick(onClick))
        filter = Filter(expression)
        textAnchor = TextAnchorValue.LEFT
        textColor = color
        textField = FormattedValue("{name}")
        textFont = StringListValue("IBM Plex Mono $fontFamily")
        textOffset = DoubleListValue(LABEL_OFFSET_LEFT, LABEL_OFFSET_RIGHT)
        textOptional = BooleanValue(!selected)
        textSize = DoubleValue(fontSize)
    }
}

@OptIn(MapboxExperimental::class)
private fun onGeoPointClick(
    onClick: (Int, Geometry) -> Unit
): FeaturesetFeatureScope.(FeaturesetFeature<FeatureState>, InteractionContext) -> Boolean =
    { featureSet, _ ->
        onClick(featureSet.properties.getInt(PROPERTY_ID), featureSet.geometry)
        true
    }
