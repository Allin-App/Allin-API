package allin.data

import allin.model.User

interface FriendDataSource {
    fun addFriend(sender: String, receiver: String)
    fun getFriendFromUserId(id: String): List<User>
    fun getFriendFromUsername(username: String)
    fun deleteFriend(senderId: String, receiverId: String)
    fun isFriend(firstUser: String, secondUser: String)
}