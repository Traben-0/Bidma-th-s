package com.traben.bidmaths.leaderboard

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "leader_board")
data class LeaderboardEntry(
    @PrimaryKey val name: String,
    @ColumnInfo(name = "score")val score: Int,
    @ColumnInfo(name = "details")val details: String
){
    override fun toString(): String {
        return "$name\n - Score:$score\n\n$details]"
    }
}