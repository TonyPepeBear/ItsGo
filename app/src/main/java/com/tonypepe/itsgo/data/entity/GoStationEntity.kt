package com.tonypepe.itsgo.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mapbox.geojson.Point

@Entity(tableName = "gostation")
data class GoStationEntity(
    @PrimaryKey
    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "addr")
    val addr: String?,

    @ColumnInfo(name = "lng")
    val lng: String?,

    @ColumnInfo(name = "lat")
    val lat: String?,
) {
    fun toPoint() = Point.fromLngLat(lng!!.toDouble(), lat!!.toDouble())
}
