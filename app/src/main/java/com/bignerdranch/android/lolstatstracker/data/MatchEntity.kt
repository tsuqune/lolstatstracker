import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "matches", foreignKeys = [
    ForeignKey(
        entity = PlayerEntity::class,
        parentColumns = ["puuid"],
        childColumns = ["playerPuuid"],
        onDelete = ForeignKey.CASCADE
    )
])
data class MatchEntity(
    @PrimaryKey val matchId: String,
    val playerPuuid: String,
    val championId: Long,
    val kills: Int,
    val deaths: Int,
    val assists: Int,
    val win: Boolean
)