package com.tonypepe.itsgo.ui

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.RenderedQueryGeometry
import com.mapbox.maps.RenderedQueryOptions
import com.mapbox.maps.Style
import com.mapbox.maps.extension.localization.localizeLabels
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.generated.circleLayer
import com.mapbox.maps.extension.style.layers.generated.fillLayer
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.mapbox.maps.extension.style.sources.getSourceAs
import com.mapbox.maps.extension.style.style
import com.mapbox.maps.plugin.animation.flyTo
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.mapbox.turf.TurfMeta
import com.tonypepe.itsgo.data.viewmodel.MainViewModel
import com.tonypepe.itsgo.databinding.ActivityOldMainBinding

class OldMainActivity : AppCompatActivity() {
    lateinit var binding: ActivityOldMainBinding
    val model: MainViewModel by viewModels()
    val TAG = this::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOldMainBinding.inflate(layoutInflater)
        model.fetchGoStation()
        setContentView(binding.root)
        val mapbox = binding.mapView.getMapboxMap()
        model.goStationFeatureCollectionLiveData.observe(this) {
            mapbox.loadStyle(
                style(styleUri = Style.LIGHT) {
                    +geoJsonSource(GOGORO_SOURCE_ID) {
                        featureCollection(it)
                        cluster(false)
                    }
                    +circleLayer(layerId = GOGORO_LAYER_ID, sourceId = GOGORO_SOURCE_ID) {
                        circleRadius(10.0)
                    }
                }, onStyleLoaded = {
                    it.localizeLabels(resources.configuration.locales[0])
                }
            )
        }
        model.isochroneFeatureCollectionLiveData.observe(this) { featureCollection ->
            if (featureCollection.features() == null || mapbox.getStyle() == null) return@observe
            mapbox.getStyle {
                if (it.styleSourceExists(ISOCHRONE_SOURCE_ID)) {
                    it.getSourceAs<GeoJsonSource>(ISOCHRONE_SOURCE_ID)
                        ?.featureCollection(featureCollection)
                } else {
                    it.addSource(geoJsonSource(ISOCHRONE_SOURCE_ID) {
                        featureCollection(featureCollection)
                        cluster(false)
                    })
                }
                if (!it.styleLayerExists(ISOCHRONE_LAYER_ID)) {
                    it.addLayer(fillLayer(ISOCHRONE_LAYER_ID, ISOCHRONE_SOURCE_ID) {
                        fillOpacity(0.5)
                    })
                }
                if (featureCollection.features()!!.size > 0) {
                    val points = TurfMeta.coordAll(featureCollection, false)
                    val options = mapbox.cameraForCoordinates(
                        points,
                        padding = EdgeInsets(1.0, 1.0, 1.0, 1.0)
                    )
                    mapbox.flyTo(options)
                }
            }
        }
        mapbox.addOnMapClickListener { point ->
            mapbox.queryRenderedFeatures(
                RenderedQueryGeometry(mapbox.pixelForCoordinate(point)),
                RenderedQueryOptions(listOf(GOGORO_LAYER_ID), null)
            ) {
                if (it.error != null) {
                    Log.e(TAG, it.error!!)
                    return@queryRenderedFeatures
                }
                if (!it.value.isNullOrEmpty()) {
                    Toast.makeText(
                        this@OldMainActivity,
                        it.value!![0].feature.getStringProperty("name"),
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    model.fetchIsochrone(point, 50 * 1000)
                }
            }
            true
        }

        model.messageLiveData.observe(this) {
            if (it.isBlank()) return@observe
            Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        const val GOGORO_SOURCE_ID = "GOGORO_SOURCE_ID"
        const val GOGORO_LAYER_ID = "GOGORO_LAYER_ID"
        const val ISOCHRONE_SOURCE_ID = "ISOCHRONE_SOURCE_ID"
        const val ISOCHRONE_LAYER_ID = "ISOCHRONE_LAYER_ID"
    }
}
