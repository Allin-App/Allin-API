package allin.data.postgres.entities

import allin.model.Friend
import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.Table
import org.ktorm.schema.varchar

interface FriendEntity : Entity<FriendEntity> {
    companion object : Entity.Factory<FriendEntity>()

    var sender: String
    var receiver: String

    fun toFriend() =
        Friend(
            sender = sender,
            receiver = receiver,
        )
}

object FriendsEntity : Table<FriendEntity>("friend") {
    val sender = varchar("sender").primaryKey().bindTo { it.sender }
    val receiver = varchar("receiver").primaryKey().bindTo { it.receiver }
}

val Database.friends get() = this.sequenceOf(FriendsEntity)