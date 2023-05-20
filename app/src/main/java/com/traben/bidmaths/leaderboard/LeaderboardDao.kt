package com.traben.bidmaths.leaderboard

import androidx.room.*

@Dao
interface LeaderboardDao {
    @Query("SELECT * FROM leader_board")
    fun getAllData(): List<LeaderboardEntry>

    @Query("SELECT * FROM leader_board ORDER BY score DESC;")
    fun getAllDataScoreOrdered(): List<LeaderboardEntry>

    @Query("DELETE FROM leader_board;")
    fun clearAllEntries()
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertData(data: LeaderboardEntry)

    @Update
    fun updateData(data: LeaderboardEntry)

    @Delete
    fun deleteData(data: LeaderboardEntry)
}