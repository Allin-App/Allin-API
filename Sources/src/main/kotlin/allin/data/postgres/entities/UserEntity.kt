package allin.data.postgres.entities

import allin.data.postgres.PostgresBetDataSource
import allin.data.postgres.PostgresFriendDataSource
import allin.data.postgres.PostgresParticipationDataSource
import allin.dto.UserDTO
import allin.model.FriendStatus
import allin.routing.imageManagerUser
import allin.utils.AppConfig
import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.entity.Entity
import org.ktorm.entity.find
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.timestamp
import org.ktorm.schema.varchar
import java.time.Instant

interface UserEntity : Entity<UserEntity> {
    companion object : Entity.Factory<UserEntity>()

    var id: String
    var username: String
    var email: String
    var password: String
    var nbCoins: Int
    var lastGift: Instant

    fun toUserDTO(database: Database, friendStatus: FriendStatus? = null) =
        UserDTO(
            id = id,
            username = username,
            email = email,
            nbCoins = nbCoins,
            token = null,
            image = getImage(id, database),
            nbBets = PostgresBetDataSource(database).getHistory(username).count(),
            nbFriends = PostgresFriendDataSource(database).getFriendFromUserId(id).count(),
            bestWin = PostgresParticipationDataSource(database).getBestWinFromUserid(id)?: 0,
            friendStatus = friendStatus
        )

    fun getImage(userId: String, database: Database): String? {
        val imageByte = database.usersimage.find { it.id eq id }?.image ?: return null
        val urlfile = "images/$userId"
        if (!imageManagerUser.imageAvailable(urlfile)) {
            imageManagerUser.saveImage(urlfile, imageByte)
        }
        return "${AppConfig.urlManager.getURL()}users/${urlfile}"
    }
}

object UsersEntity : Table<UserEntity>("users") {
    val id = varchar("id").primaryKey().bindTo { it.id }
    val username = varchar("username").bindTo { it.username }
    val password = varchar("password").bindTo { it.password }
    val nbCoins = int("coins").bindTo { it.nbCoins }
    val email = varchar("email").bindTo { it.email }
    val lastGift = timestamp("lastgift").bindTo { it.lastGift }
}

val Database.users get() = this.sequenceOf(UsersEntity)

