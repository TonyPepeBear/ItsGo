package com.tonypepe.itsgo.data.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.tonypepe.itsgo.data.AppDatabase
import com.tonypepe.itsgo.data.entity.GoStation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val db by lazy { AppDatabase.getInstance(getApplication<Application>().applicationContext) }
    private val httpClient by lazy { OkHttpClient() }
    val featureCollectionLiveData: LiveData<FeatureCollection> =
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

    fun fetchGoStation() {
        viewModelScope.launch(Dispatchers.IO) {
            val req = Request.Builder()
                .url("https://github.com/tonypepebear/gogoroapi/releases/latest/download/go-station.csv")
                .build()
            val s = httpClient.newCall(req).execute().body?.string() ?: return@launch
            val arr = s.split("\n")
                .drop(1)
                .map { it.split(",") }
                .filter { it.size == 4 }
                .map { GoStation(it[0], it[1], it[2], it[3]) }
                .toTypedArray()
            db.goStationDao().deleteAll()
            db.goStationDao().insertAll(*arr)
        }
    }
}
