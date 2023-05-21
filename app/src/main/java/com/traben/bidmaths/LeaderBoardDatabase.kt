package com.traben.bidmaths

import android.content.Context
import androidx.room.*

/**
 * A file containing all the classes used for the LeaderBoard database
 * they are collected in the one file as they are highly related and very minimalistic classes
 * and reduces file clutter
* */

@Database(entities = [LeaderboardEntry::class], version = 1)
abstract class LeaderBoard : RoomDatabase() {
    abstract fun getDao(): LeaderboardDao

    companion object{
        fun getDatabase(context: Context) : LeaderBoard{
            return Room.databaseBuilder(context, LeaderBoard::class.java, "leader-board")
                .build()
        }
    }
}
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

@Entity(tableName = "leader_board")
data class LeaderboardEntry(
    @PrimaryKey val name: String,
    @ColumnInfo(name = "score") val score: Int,
    @ColumnInfo(name = "details") val details: String
) {
    override fun toString(): String {
        return "$name\n - Score:$score\n\n$details]"
    }
}


