package allin.data.postgres.entities

import org.ktorm.entity.Entity
import org.ktorm.schema.Table
import org.ktorm.schema.varchar

interface FriendEntity : Entity<FriendEntity> {
    val sender: String
    val receiver: String
}

object FriendsEntity : Table<FriendEntity>("friend") {
    val sender = varchar("id").primaryKey().bindTo { it.sender }
    val receiver = varchar("bet").primaryKey().bindTo { it.receiver }
}