package com.traben.bidmaths.leaderboard

import androidx.room.*

@Dao
interface LeaderboardDao {
    @Query("SELECT * FROM leader_board")
    fun getAllData(): List<LeaderboardEntry>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertData(data: LeaderboardEntry)

    @Update
    fun updateData(data: LeaderboardEntry)

    @Delete
    fun deleteData(data: LeaderboardEntry)
}