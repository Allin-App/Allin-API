package allin.data

import allin.dto.UserDTO

interface FriendDataSource {
    fun addFriend(sender: String, receiver: String)
    fun getFriendFromUserId(id: String): List<UserDTO>
    fun deleteFriend(senderId: String, receiverId: String): Boolean
    fun isFriend(firstUser: String, secondUser: String): Boolean
    fun filterUsersByUsername(fromUserId: String, search: String): List<UserDTO>
}