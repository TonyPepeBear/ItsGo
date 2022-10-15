package com.tonypepe.itsgo.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tonypepe.itsgo.data.entity.SettingEntity

@Dao
interface SettingDao {
    @Query("SELECT * FROM SETTING WHERE `KEY` = :key")
    fun getSetting(key: String): SettingEntity

    @Query("SELECT * FROM SETTING WHERE `KEY` = :key")
    fun getSettingLiveData(key: String): LiveData<SettingEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun setSetting(settingEntity: SettingEntity)
}
