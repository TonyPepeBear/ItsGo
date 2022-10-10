package com.tonypepe.itsgo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mapbox.maps.Style
import com.mapbox.maps.extension.localization.localizeLabels
import com.tonypepe.itsgo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.mapView.getMapboxMap().apply {
            loadStyleUri(Style.SATELLITE_STREETS) {
                it.localizeLabels(resources.configuration.locales[0])
            }
        }
    }
}