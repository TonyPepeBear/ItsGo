package com.tonypepe.itsgo.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "SETTING")
data class SettingEntity(
    @PrimaryKey
    @ColumnInfo(name = "KEY")
    val key: String,
    @ColumnInfo(name = "VALUE")
    val value: String,
) {
    companion object {
        const val KEY_SETTING_ISOCHRONE = "SETTING_ISOCHRONE" // INT
    }
}
