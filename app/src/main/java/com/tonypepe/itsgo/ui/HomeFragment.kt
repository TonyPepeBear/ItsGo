package com.tonypepe.itsgo.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.snackbar.Snackbar
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.maps.*
import com.mapbox.maps.extension.localization.localizeLabels
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.generated.symbolLayer
import com.mapbox.maps.extension.style.layers.properties.generated.IconAnchor
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.mapbox.maps.extension.style.sources.getSourceAs
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.flyTo
import com.mapbox.maps.plugin.gestures.OnMapClickListener
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.mapbox.maps.plugin.locationcomponent.location
import com.tonypepe.itsgo.R
import com.tonypepe.itsgo.data.viewmodel.MainViewModel
import com.tonypepe.itsgo.databinding.FragmentHomeBinding

class HomeFragment : Fragment(), OnMapClickListener, PermissionsListener {
    val TAG = this::class.simpleName

    lateinit var binding: FragmentHomeBinding

    val model: MainViewModel by activityViewModels()

    lateinit var mapbox: MapboxMap

    lateinit var permissionsManager: PermissionsManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        mapbox = binding.mapView.getMapboxMap()
        permissionsManager = PermissionsManager(this)
        // init mapbox
        mapbox.loadStyleUri(Style.MAPBOX_STREETS) { style ->
            style.localizeLabels(resources.configuration.locales[0])
            // battery img
            style.addImage(
                BATTERY_IMG_ID,
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_battery_solid)!!
                    .toBitmap()
            )
            // add empty source
            style.addSource(geoJsonSource(GO_STATION_SOURCE_ID) {
                featureCollection(FeatureCollection.fromFeatures(emptyArray()))
                cluster(false)
            })
            style.addLayer(symbolLayer(GO_STATION_LAYER_ID, GO_STATION_SOURCE_ID) {
                sourceLayer(GO_STATION_SOURCE_ID)
                iconImage(BATTERY_IMG_ID)
                iconAllowOverlap(true)
                iconAnchor(IconAnchor.BOTTOM)
            })
        }
        mapbox.addOnMapClickListener(this)
        // observe go station livedata to mapbox source
        model.goStationFeatureCollectionLiveData.observe(viewLifecycleOwner) { collection ->
            mapbox.getStyle { style ->
                style.getSourceAs<GeoJsonSource>(GO_STATION_SOURCE_ID)!!
                    .featureCollection(collection)
            }
        }
        if (PermissionsManager.areLocationPermissionsGranted(context)) {
            Log.d(TAG, "onCreateView: HI")
        } else {
            permissionsManager.requestLocationPermissions(activity)
        }
        mapbox.addOnStyleLoadedListener {
            binding.mapView.location.updateSettings {
                enabled = true
                pulsingEnabled = true
            }
        }
        // fly to location
        model.flyToLocation.observe(viewLifecycleOwner) { point ->
            if (point != null) {
                val cameraOptions = CameraOptions.Builder()
                    .center(point)
                    .zoom(15.0)
                    .build()
                val animationOptions = MapAnimationOptions.mapAnimationOptions {
                    duration(3 * 1000)
                }
                mapbox.flyTo(cameraOptions, animationOptions)
            }
        }
        return binding.root
    }

    override fun onMapClick(point: Point): Boolean {
        mapbox.queryRenderedFeatures(
            RenderedQueryGeometry(mapbox.pixelForCoordinate(point)),
            RenderedQueryOptions(listOf(OldMainActivity.GOGORO_LAYER_ID), null)
        ) {
            if (it.error != null) {
                Log.e(TAG, it.error!!)
                return@queryRenderedFeatures
            }
            if (!it.value.isNullOrEmpty()) {
                Snackbar.make(
                    binding.root,
                    it.value!![0].feature.getStringProperty("name"),
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
        return true
    }

    override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {}

    override fun onPermissionResult(granted: Boolean) {}

    companion object {
        const val BATTERY_IMG_ID = "BATTERY_IMG_ID"
        const val GO_STATION_SOURCE_ID = "GOGORO_SOURCE_ID"
        const val GO_STATION_LAYER_ID = "GOGORO_LAYER_ID"
        const val ISOCHRONE_SOURCE_ID = "ISOCHRONE_SOURCE_ID"
        const val ISOCHRONE_LAYER_ID = "ISOCHRONE_LAYER_ID"
    }
}
