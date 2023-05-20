package com.traben.bidmaths.leaderboard

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [LeaderboardEntry::class], version = 1)
abstract class LeaderBoard : RoomDatabase() {
    abstract fun getDao(): LeaderboardDao


}


