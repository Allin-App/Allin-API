package allin.data.postgres.entities

import allin.dto.UserDTO
import allin.model.FriendStatus
import allin.utils.AppConfig
import org.ktorm.database.Database
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.entity.*
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
            image = AppConfig.imageManager.getImage(id, database),
            nbBets = database.participations.count { it.userid eq this.id },
            nbFriends = database.friends
                .filter { it.receiver eq this.id }
                .mapNotNull { p -> database.friends.any { (it.sender eq this.id) and (it.receiver eq p.sender) } }
                .count(),
            bestWin = database.participations
                .filter { (it.id eq this.id) }
                .mapNotNull { p ->
                    if (database.betResults.any { (it.betId eq p.bet.id) and (it.result eq p.answer) }) {
                        p.stake
                    } else null
                }
                .maxOrNull() ?: 0,
            friendStatus = friendStatus
        )

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

