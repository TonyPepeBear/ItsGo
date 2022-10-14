package com.tonypepe.itsgo.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.mapbox.geojson.Feature
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.Style
import com.mapbox.maps.extension.localization.localizeLabels
import com.mapbox.maps.extension.observable.eventdata.MapLoadedEventData
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.generated.symbolLayer
import com.mapbox.maps.extension.style.layers.properties.generated.IconAnchor
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.mapbox.maps.extension.style.sources.getSourceAs
import com.mapbox.maps.plugin.delegates.listeners.OnMapLoadedListener
import com.tonypepe.itsgo.R
import com.tonypepe.itsgo.data.viewmodel.MainViewModel
import com.tonypepe.itsgo.databinding.FragmentGoStationDetailBinding

class GoStationDetailFragment : Fragment(), OnMapLoadedListener {
    lateinit var binding: FragmentGoStationDetailBinding
    val model: MainViewModel by activityViewModels()
    private lateinit var mapbox: MapboxMap

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGoStationDetailBinding.inflate(inflater, container, false)
        mapbox = binding.mapView.getMapboxMap()
        mapbox.loadStyleUri(Style.MAPBOX_STREETS) { style ->
            style.localizeLabels(resources.configuration.locales[0])
            style.addSource(geoJsonSource(GO_STATION_SOURCE_ID))
            style.addImage(
                GO_STATION_IMG_ID,
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_battery_solid)!!
                    .toBitmap()
            )
            style.addLayer(symbolLayer(GO_STATION_LAYER_ID, GO_STATION_SOURCE_ID) {
                sourceLayer(GO_STATION_SOURCE_ID)
                iconImage(GO_STATION_IMG_ID)
                iconAllowOverlap(true)
                iconAnchor(IconAnchor.BOTTOM)
            })
        }
        mapbox.addOnMapLoadedListener(this)
        model.showDetail.observe(viewLifecycleOwner) {
            if (it != null) {
                binding.title.text = it.name
                binding.address.text = it.addr
                // camera
                mapbox.setCamera(
                    CameraOptions.Builder()
                        .center(it.toPoint())
                        .zoom(15.5)
                        .build()
                )
            }
        }
        return binding.root
    }

    override fun onMapLoaded(eventData: MapLoadedEventData) {
        model.showDetail.observe(viewLifecycleOwner) {
            if (it != null) {
                mapbox.getStyle()!!.getSourceAs<GeoJsonSource>(GO_STATION_SOURCE_ID)!!.feature(
                    Feature.fromGeometry(it.toPoint())
                )
            }
        }
    }

    companion object {
        const val GO_STATION_LAYER_ID = "GO_STATION_LAYER_ID"
        const val GO_STATION_SOURCE_ID = "GO_STATION_SOURCE_ID"
        const val GO_STATION_IMG_ID = "GO_STATION_IMG_ID"
    }
}
