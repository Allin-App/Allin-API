package allin.data.postgres

import allin.data.FriendDataSource
import allin.data.postgres.entities.FriendEntity
import allin.data.postgres.entities.friends
import allin.data.postgres.entities.users
import allin.dto.UserDTO
import allin.ext.toLowerCase
import allin.model.FriendStatus
import org.ktorm.database.Database
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.dsl.like
import org.ktorm.dsl.notEq
import org.ktorm.entity.*

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
            .mapNotNull {
                database.users.find { usr ->
                    usr.id eq it.receiver
                }?.toUserDTO(
                    friendStatus = if (isFriend(it.receiver, id)) {
                        FriendStatus.FRIEND
                    } else FriendStatus.REQUESTED
                )
            }


    override fun deleteFriend(senderId: String, receiverId: String): Boolean {
        database.friends.removeIf { (it.sender eq receiverId) and (it.receiver eq senderId) }
        return database.friends.removeIf { (it.sender eq senderId) and (it.receiver eq receiverId) } > 0
    }

    override fun isFriend(firstUser: String, secondUser: String) =
        database.friends.any { (it.sender eq firstUser) and (it.receiver eq secondUser) }

    override fun filterUsersByUsername(fromUserId: String, search: String): List<UserDTO> =
        database.users
            .filter { (it.username.toLowerCase() like "%$search%") and (it.id notEq fromUserId) }
            .map { user -> user.toUserDTO(friendStatus = getFriendStatus(fromUserId, user.id)) }
}