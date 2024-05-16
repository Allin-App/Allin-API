package allin.data.postgres

import allin.data.FriendDataSource
import allin.data.postgres.entities.FriendEntity
import allin.data.postgres.entities.friends
import org.ktorm.database.Database
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.entity.add
import org.ktorm.entity.filter
import org.ktorm.entity.map
import org.ktorm.entity.removeIf

class PostgresFriendDataSource(private val database: Database) : FriendDataSource {
    override fun addFriend(sender: String, receiver: String) {
        database.friends.add(
            FriendEntity {
                this.sender = sender
                this.receiver = receiver
            }
        )
    }

    override fun getFriendFromUserId(id: String) =
        database.friends.map { it.toFriend() }
            .filter { it.sender == id }
            .map { it.receiver }


    override fun deleteFriend(senderId: String, receiverId: String): Boolean {
        database.friends.removeIf { (it.sender eq senderId) and (it.receiver eq receiverId) }
        return database.friends.removeIf { (it.sender eq senderId) and (it.receiver eq receiverId) } > 0
    }

    override fun isFriend(firstUser: String, secondUser: String) =
        database.friends
            .filter { (it.sender eq firstUser) and (it.receiver eq secondUser) }
            .map { it.toFriend() }
            .isNotEmpty()
}