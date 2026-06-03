package com.example.data.db

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "games_history")
data class GameHistoryEntity(
    @PrimaryKey val title: String,
    val creator: String,
    val releaseDate: String,
    val genreJson: String, // Moshi serialized List<String>
    val jsonContent: String, // Moshi serialized GameReport
    val savedAt: Long = System.currentTimeMillis()
)

@Dao
interface GameHistoryDao {
    @Query("SELECT * FROM games_history ORDER BY savedAt DESC")
    fun getAllHistory(): Flow<List<GameHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGame(game: GameHistoryEntity)

    @Query("DELETE FROM games_history WHERE title = :title")
    suspend fun deleteGame(title: String)

    @Query("DELETE FROM games_history")
    suspend fun clearAll()
}

@Database(entities = [GameHistoryEntity::class], version = 1, exportSchema = false)
abstract class GameDatabase : RoomDatabase() {
    abstract fun gameHistoryDao(): GameHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: GameDatabase? = null

        fun getDatabase(context: Context): GameDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GameDatabase::class.java,
                    "games_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
