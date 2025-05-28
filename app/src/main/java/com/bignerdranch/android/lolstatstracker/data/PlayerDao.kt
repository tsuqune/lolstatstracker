import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PlayerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayer(player: PlayerEntity)

    @Query("SELECT * FROM players WHERE puuid = :puuid")
    suspend fun getPlayer(puuid: String): PlayerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatches(matches: List<MatchEntity>)

    @Query("SELECT * FROM matches WHERE playerPuuid = :puuid")
    suspend fun getMatchesForPlayer(puuid: String): List<MatchEntity>

    @Query("DELETE FROM players")
    suspend fun clearPlayers()

    @Query("DELETE FROM matches")
    suspend fun clearMatches()
}