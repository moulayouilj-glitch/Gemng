package com.example.data

import com.example.data.db.GameHistoryDao
import com.example.data.db.GameHistoryEntity
import kotlinx.coroutines.flow.Flow

class GameRepository(private val dao: GameHistoryDao) {
    val history: Flow<List<GameHistoryEntity>> = dao.getAllHistory()

    suspend fun insertGame(game: GameHistoryEntity) {
        dao.insertGame(game)
    }

    suspend fun deleteGame(title: String) {
        dao.deleteGame(title)
    }

    suspend fun clearHistory() {
        dao.clearAll()
    }
}
