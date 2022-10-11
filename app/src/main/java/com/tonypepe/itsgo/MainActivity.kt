package com.tonypepe.itsgo

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
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
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.tonypepe.itsgo.data.viewmodel.MainViewModel
import com.tonypepe.itsgo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    val model: MainViewModel by viewModels()
    val TAG = this::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
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
                        this@MainActivity,
                        it.value!![0].feature.getStringProperty("name"),
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    model.fetchIsochrone(point, 50 * 1000)
                }
            }
            true
        }
    }

    companion object {
        const val GOGORO_SOURCE_ID = "GOGORO_SOURCE_ID"
        const val GOGORO_LAYER_ID = "GOGORO_LAYER_ID"
        const val ISOCHRONE_SOURCE_ID = "ISOCHRONE_SOURCE_ID"
        const val ISOCHRONE_LAYER_ID = "ISOCHRONE_LAYER_ID"
    }
}
