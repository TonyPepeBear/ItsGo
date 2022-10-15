package com.tonypepe.itsgo.data

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.map
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.tonypepe.itsgo.data.dao.GoStationDao
import com.tonypepe.itsgo.data.dao.SettingDao
import com.tonypepe.itsgo.data.entity.GoStationEntity
import com.tonypepe.itsgo.data.entity.SettingEntity

@Database(entities = [GoStationEntity::class, SettingEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun goStationDao(): GoStationDao
    abstract fun settingDao(): SettingDao

    fun setIsochroneSetting(i: Int) {
        settingDao().setSetting(
            SettingEntity(
                SettingEntity.KEY_SETTING_ISOCHRONE,
                i.toString()
            )
        )
    }

    fun getIsochroneSettingLiveData(): LiveData<Int> = liveData {
        emitSource(
            settingDao()
                .getSettingLiveData(SettingEntity.KEY_SETTING_ISOCHRONE)
                .map { it.value.toInt() }
        )
    }

    companion object {
        private var instance: AppDatabase? = null
        fun getInstance(context: Context): AppDatabase {
            if (instance == null) {
                instance = Room.databaseBuilder(context, AppDatabase::class.java, "app-db")
                    .build()
            }
            return instance!!
        }
    }
}