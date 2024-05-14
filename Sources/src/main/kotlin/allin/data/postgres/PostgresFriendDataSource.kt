package allin.data.postgres

import allin.data.FriendDataSource
import org.ktorm.database.Database

class PostgresFriendDataSource(private val database: Database) : FriendDataSource {
    override fun addFriend(sender: String, receiver: String) {
//        TODO("Not yet implemented")
    }

    override fun getFriendFromUserId(id: String) {
        TODO("Not yet implemented")
    }

    override fun getFriendFromUsername(username: String) {
        TODO("Not yet implemented")
    }

    override fun deleteFriend(senderId: String, receiverId: String) {
        TODO("Not yet implemented")
    }

    override fun isFriend(firstUser: String, secondUser: String) {
        TODO("Not yet implemented")
    }

}