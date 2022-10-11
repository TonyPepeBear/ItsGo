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
import com.mapbox.maps.extension.style.layers.generated.circleLayer
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
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
        model.featureCollectionLiveData.observe(this) {
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
        mapbox.addOnMapClickListener { point ->
            mapbox.queryRenderedFeatures(
                RenderedQueryGeometry(mapbox.pixelForCoordinate(point)),
                RenderedQueryOptions(listOf(GOGORO_LAYER_ID), null)
            ) {
                if (it.error != null) {
                    Log.e(TAG, it.error!!)
                    return@queryRenderedFeatures
                }
                Log.d(TAG, it.value!!.size.toString())
                if (!it.value.isNullOrEmpty()) {
                    Toast.makeText(
                        this@MainActivity,
                        it.value!![0].feature.getStringProperty("name"),
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }
            true
        }
    }

    companion object {
        const val GOGORO_SOURCE_ID = "GOGORO_SOURCE_ID"
        const val GOGORO_LAYER_ID = "GOGORO_LAYER_ID"
    }
}
