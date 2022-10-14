package com.tonypepe.itsgo

import android.location.Location
import com.mapbox.geojson.Point

fun Location.toPoint() = Point.fromLngLat(this.longitude, this.latitude)
