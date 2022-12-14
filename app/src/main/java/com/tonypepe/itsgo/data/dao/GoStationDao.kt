package com.tonypepe.itsgo.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tonypepe.itsgo.data.entity.GoStationEntity

@Dao
interface GoStationDao {
    @Query("SELECT * FROM gostation")
    fun getAll(): List<GoStationEntity>

    @Query("SELECT * FROM gostation")
    fun getAllLiveData(): LiveData<List<GoStationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg goStations: GoStationEntity)

    @Query("DELETE FROM gostation")
    fun deleteAll(): Int

    @Query("SELECT * FROM GOSTATION WHERE name = :name")
    fun getOneWithName(name: String): GoStationEntity
}
