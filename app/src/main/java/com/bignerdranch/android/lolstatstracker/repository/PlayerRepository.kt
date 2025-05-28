import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PlayerRepository(private val playerDao: PlayerDao) {
    suspend fun savePlayerData(player: PlayerEntity, matches: List<MatchEntity>) {
        withContext(Dispatchers.IO) {
            playerDao.insertPlayer(player)
            playerDao.insertMatches(matches)
        }
    }

    suspend fun getCachedPlayer(puuid: String): PlayerEntity? {
        return withContext(Dispatchers.IO) {
            playerDao.getPlayer(puuid)
        }
    }

    suspend fun getCachedMatches(puuid: String): List<MatchEntity> {
        return withContext(Dispatchers.IO) {
            playerDao.getMatchesForPlayer(puuid)
        }
    }

    suspend fun clearCache() {
        withContext(Dispatchers.IO) {
            playerDao.clearPlayers()
            playerDao.clearMatches()
        }
    }
}