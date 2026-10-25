package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WalkDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(walk: WalkEntity): Long

    @Update
    suspend fun update(walk: WalkEntity)

    @Delete
    suspend fun delete(walk: WalkEntity)

    @Query("DELETE FROM walks")
    suspend fun deleteAll()

    @Query("SELECT * FROM walks ORDER BY startTimeMillis DESC")
    fun getAllFlow(): Flow<List<WalkEntity>>

    @Query("SELECT * FROM walks WHERE id = :id LIMIT 1")
    fun getByIdFlow(id: Long): Flow<WalkEntity?>

    @Query("SELECT * FROM walks ORDER BY startTimeMillis DESC LIMIT :limit")
    fun getRecentFlow(limit: Int): Flow<List<WalkEntity>>

    @Query("SELECT * FROM walks WHERE startTimeMillis >= :startOfDayMillis ORDER BY startTimeMillis DESC")
    fun getWalksSinceFlow(startOfDayMillis: Long): Flow<List<WalkEntity>>
}
