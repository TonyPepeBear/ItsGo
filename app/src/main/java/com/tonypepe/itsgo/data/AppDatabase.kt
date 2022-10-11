package com.tonypepe.itsgo.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.tonypepe.itsgo.data.dao.GoStationDao
import com.tonypepe.itsgo.data.entity.GoStation

@Database(entities = [GoStation::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun goStationDao(): GoStationDao

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