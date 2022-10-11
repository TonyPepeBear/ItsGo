package com.tonypepe.itsgo.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tonypepe.itsgo.data.entity.GoStation

@Dao
interface GoStationDao {
    @Query("SELECT * FROM gostation")
    fun getAll(): List<GoStation>

    @Query("SELECT * FROM gostation")
    fun getAllLiveData(): LiveData<List<GoStation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg goStations: GoStation)

    @Query("DELETE FROM gostation")
    fun deleteAll(): Int
}
