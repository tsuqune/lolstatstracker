import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "players")
data class PlayerEntity(
    @PrimaryKey val puuid: String,
    val gameName: String,
    val tagLine: String,
    val summonerName: String,
    val summonerLevel: Int,
    val profileIconId: Int,
    val rank: String?,
    val tier: String?,
    val leaguePoints: Int?,
    val wins: Int?,
    val losses: Int?,
    val winRate: Double?,
    val lastUpdated: Long = System.currentTimeMillis()
)