package com.traben.bidmaths.leaderboard

import androidx.room.*

@Database(entities = [LeaderboardEntry::class], version = 1)
abstract class LeaderBoard: RoomDatabase() {
    abstract fun getDao(): LeaderboardDao


}


