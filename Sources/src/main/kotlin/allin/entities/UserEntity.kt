package allin.entities

import org.ktorm.entity.Entity
import org.ktorm.schema.*

interface UserEntity : Entity<UserEntity> {
    val username: String
    var email: String
    var password: String
    var nbCoins: Int
}

object UsersEntity : Table<UserEntity>("utilisateur") {
    val id = uuid("id").primaryKey()
    val username = varchar("username")
    val password = varchar("password")
    val nbCoins = int("coins")
    val email = varchar("email")
    val lastGift = timestamp("lastgift")
}

