package com.tonypepe.itsgo

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.mapbox.maps.Style
import com.mapbox.maps.extension.localization.localizeLabels
import com.mapbox.maps.extension.style.layers.generated.circleLayer
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.mapbox.maps.extension.style.style
import com.tonypepe.itsgo.data.viewmodel.MainViewModel
import com.tonypepe.itsgo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    val model: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        model.fetchGoStation()
        setContentView(binding.root)
        model.featureCollectionLiveData.observe(this) {
            binding.mapView.getMapboxMap().loadStyle(
                style(styleUri = Style.LIGHT) {
                    +geoJsonSource(GOGORO_SOURCE_ID) {
                        featureCollection(it)
                        cluster(false)
                    }
                    +circleLayer(layerId = GOGORO_LAYER_ID, sourceId = GOGORO_SOURCE_ID) {

                    }
                }, onStyleLoaded = {
                    it.localizeLabels(resources.configuration.locales[0])
                }
            )
        }
    }

    companion object {
        const val GOGORO_SOURCE_ID = "GOGORO_SOURCE_ID"
        const val GOGORO_LAYER_ID = "GOGORO_LAYER_ID"
    }
}
