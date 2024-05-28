package allin.data.postgres.entities

import allin.dto.UserDTO
import allin.model.FriendStatus
import org.ktorm.database.Database
import org.ktorm.entity.Entity
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

    fun toUserDTO(friendStatus: FriendStatus? = null) =
        UserDTO(
            id = id,
            username = username,
            email = email,
            nbCoins = nbCoins,
            token = null,
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

