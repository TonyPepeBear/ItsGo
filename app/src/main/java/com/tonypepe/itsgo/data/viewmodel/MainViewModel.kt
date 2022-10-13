package com.tonypepe.itsgo.data.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.google.gson.JsonObject
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.tonypepe.itsgo.R
import com.tonypepe.itsgo.data.AppDatabase
import com.tonypepe.itsgo.data.entity.GoStation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.*
import java.io.IOException

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = this::class.java.simpleName

    private val db by lazy { AppDatabase.getInstance(getApplication<Application>().applicationContext) }

    private val httpClient by lazy { OkHttpClient() }

    val goStationCountLiveData = liveData(Dispatchers.IO) {
        emitSource(db.goStationDao().getAllLiveData().map { it.size })
    }

    val flyToLocation: MutableLiveData<Point> = MutableLiveData(null)

    val goStationFeatureCollectionLiveData: LiveData<FeatureCollection> =
        db.goStationDao().getAllLiveData().map {
            FeatureCollection.fromFeatures(it.map {
                Feature.fromGeometry(
                    Point.fromLngLat(
                        it.lng!!.toDouble(),
                        it.lat!!.toDouble()
                    ),
                    JsonObject().apply { addProperty("name", it.name) }
                )
            })
        }

    private val _isochroneFeatureCollectionLiveData = MutableLiveData(
        FeatureCollection.fromFeatures(
            emptyArray()
        )
    )
    val isochroneFeatureCollectionLiveData: LiveData<FeatureCollection> get() = _isochroneFeatureCollectionLiveData

    private val _toastMessageLiveData: MutableLiveData<String> = MutableLiveData("")
    val toastMessageLiveData: LiveData<String> get() = _toastMessageLiveData

    private val resources
        get() = getApplication<Application>().resources

    fun fetchGoStation() {
        viewModelScope.launch(Dispatchers.IO) {
            db.goStationDao().deleteAll()
            val req = Request.Builder()
                .url("https://github.com/tonypepebear/gogoroapi/releases/latest/download/go-station.csv")
                .build()
            httpClient.newCall(req).enqueue(object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    val s = response.body!!.string()
                    val arr = s.split("\n")
                        .drop(1)
                        .map { it.split(",") }
                        .filter { it.size == 4 }
                        .map { GoStation(it[0], it[1], it[2], it[3]) }
                        .toTypedArray()
                    db.goStationDao().deleteAll()
                    db.goStationDao().insertAll(*arr)
                }

                override fun onFailure(call: Call, e: IOException) {
                    _toastMessageLiveData.postValue("Network error")
                }
            })
        }
    }

    fun fetchIsochrone(point: Point, meters: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val req = Request.Builder()
                .url(createIsochroneURL(point, meters))
                .build()
            httpClient.newCall(req).enqueue(object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    val resStr = response.body!!.string()
                    val featureCollection = FeatureCollection.fromJson(resStr)
                    _isochroneFeatureCollectionLiveData.postValue(featureCollection)
                }

                override fun onFailure(call: Call, e: IOException) {
                    _toastMessageLiveData.postValue("Network Error")
                }
            })
        }
    }

    fun createIsochroneURL(point: Point, meters: Int): String =
        "https://api.mapbox.com/isochrone/v1/mapbox/driving/" +
                "${point.longitude()},${point.latitude()}?" +
                "contours_meters=$meters&" +
                "polygons=true&" +
                "denoise=1&" +
                "access_token=${resources.getString(R.string.mapbox_access_token)}"
}
