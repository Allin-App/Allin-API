package allin.entities

import org.ktorm.entity.Entity
import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.uuid
import org.ktorm.schema.varchar

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
}


