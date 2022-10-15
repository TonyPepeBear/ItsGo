package com.tonypepe.itsgo.ui.fragment

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.maps.*
import com.mapbox.maps.extension.localization.localizeLabels
import com.mapbox.maps.extension.observable.eventdata.CameraChangedEventData
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.generated.fillLayer
import com.mapbox.maps.extension.style.layers.generated.symbolLayer
import com.mapbox.maps.extension.style.layers.properties.generated.IconAnchor
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.mapbox.maps.extension.style.sources.getSourceAs
import com.mapbox.maps.plugin.animation.MapAnimationOptions.Companion.mapAnimationOptions
import com.mapbox.maps.plugin.animation.flyTo
import com.mapbox.maps.plugin.delegates.listeners.OnCameraChangeListener
import com.mapbox.maps.plugin.gestures.OnMapClickListener
import com.mapbox.maps.plugin.gestures.OnMapLongClickListener
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.mapbox.maps.plugin.gestures.addOnMapLongClickListener
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.turf.TurfMeta
import com.tonypepe.itsgo.R
import com.tonypepe.itsgo.data.viewmodel.MainViewModel
import com.tonypepe.itsgo.databinding.FragmentHomeBinding
import com.tonypepe.itsgo.ui.OldMainActivity

class HomeFragment : Fragment(), OnMapClickListener, PermissionsListener, OnCameraChangeListener,
    OnMapLongClickListener {
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
        val isDarkMode =
            (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_NO
        // init mapbox
        mapbox.loadStyleUri(if (isDarkMode) Style.MAPBOX_STREETS else Style.DARK) { style ->
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
            style.addSource(geoJsonSource(ISOCHRONE_SOURCE_ID) {
                featureCollection(FeatureCollection.fromFeatures(emptyArray()))
                cluster(false)
            })
            // layer
            style.addLayer(symbolLayer(GO_STATION_LAYER_ID, GO_STATION_SOURCE_ID) {
                sourceLayer(GO_STATION_SOURCE_ID)
                iconImage(BATTERY_IMG_ID)
                iconAllowOverlap(true)
                iconAnchor(IconAnchor.BOTTOM)
            })
            style.addLayer(fillLayer(ISOCHRONE_LAYER_ID, ISOCHRONE_SOURCE_ID) {
                fillOpacity(0.5)
                fillColor("#03fc8c")
            })
        }
        // on map click, long click
        mapbox.addOnMapClickListener(this)
        mapbox.addOnMapLongClickListener(this)
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
        // map camera listener
        mapbox.addOnCameraChangeListener(this)
        // camera state
        if (model.cameraState.value != null) {
            mapbox.setCamera(model.cameraState.value!!.toCameraOptions())
        }
        // fly to location
        model.flyToLocation.observe(viewLifecycleOwner) { point ->
            if (point != null && model.isNeedToFly.value == true) {
                val cameraOptions = CameraOptions.Builder()
                    .center(point)
                    .zoom(model.flyToZoom.value)
                    .build()
                val animationOptions = mapAnimationOptions {
                    duration(3 * 1000)
                }
                mapbox.flyTo(cameraOptions, animationOptions)
                model.finishFlyTo()
            }
        }
        // observe isochrone
        model.isochroneFeatureCollectionLiveData.observe(viewLifecycleOwner) { collection ->
            val style = mapbox.getStyle() ?: return@observe
            if (TurfMeta.coordAll(collection, false).size > 0) {
                style.getSourceAs<GeoJsonSource>(ISOCHRONE_SOURCE_ID)!!
                    .featureCollection(collection)
                // camera
                val points = TurfMeta.coordAll(collection, false)
                val padding = 8.0
                val cameraOptions = mapbox.cameraForCoordinates(
                    points,
                    padding = EdgeInsets(padding, padding, padding, padding)
                )
                val animationOptions = mapAnimationOptions {
                    duration(2 * 1000)
                }
                mapbox.flyTo(cameraOptions, animationOptions)
            } else {
                style.getSourceAs<GeoJsonSource>(ISOCHRONE_SOURCE_ID)!!
                    .featureCollection(FeatureCollection.fromFeatures(emptyArray()))
            }
        }
        return binding.root
    }

    override fun onMapClick(point: Point): Boolean {
        model.clearIsochrone()
        mapbox.queryRenderedFeatures(
            RenderedQueryGeometry(mapbox.pixelForCoordinate(point)),
            RenderedQueryOptions(listOf(OldMainActivity.GOGORO_LAYER_ID), null)
        ) {
            if (it.error != null) {
                Log.e(TAG, it.error!!)
                return@queryRenderedFeatures
            }
            if (!it.value.isNullOrEmpty()) {
                val goStationName = it.value!![0].feature.getStringProperty("name")
                model.setDetailGoStationWithName(goStationName)
                findNavController().navigate(R.id.nav_go_station_detail_fragment)
            }
        }
        return true
    }

    override fun onMapLongClick(point: Point): Boolean {
        mapbox.queryRenderedFeatures(
            RenderedQueryGeometry(mapbox.pixelForCoordinate(point)),
            RenderedQueryOptions(listOf(OldMainActivity.GOGORO_LAYER_ID), null)
        ) {
            if (it.error != null) {
                Log.e(TAG, it.error!!)
                return@queryRenderedFeatures
            }
            if (!it.value.isNullOrEmpty()) {
                // do nothing
            }
        }
        model.clearIsochrone()
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

    override fun onCameraChanged(eventData: CameraChangedEventData) {
        // save camera state
        model.cameraState.postValue(mapbox.cameraState)
    }

    override fun onDestroy() {
        mapbox.removeOnCameraChangeListener(this)
        super.onDestroy()
    }
}
