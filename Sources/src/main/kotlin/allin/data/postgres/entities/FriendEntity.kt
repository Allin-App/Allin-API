package allin.data.postgres.entities

import allin.model.Friend
import allin.model.FriendStatus
import org.ktorm.database.Database
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.entity.Entity
import org.ktorm.entity.filter
import org.ktorm.entity.isEmpty
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
fun Database.getFriendStatus(ofUserId: String, withUserId: String) =
    this.friends
        .filter { (it.receiver eq withUserId) and (it.sender eq ofUserId) }
        .let {
            if (it.isEmpty()) {
                FriendStatus.NOT_FRIEND
            } else {
                this.friends
                    .filter { (it.receiver eq ofUserId) and (it.sender eq withUserId) }
                    .let {
                        if (it.isEmpty()) FriendStatus.REQUESTED
                        else FriendStatus.FRIEND
                    }
            }
        }