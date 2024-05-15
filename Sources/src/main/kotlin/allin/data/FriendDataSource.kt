package allin.data

interface FriendDataSource {
    fun addFriend(sender: String, receiver: String)
    fun getFriendFromUserId(id: String): List<String>
    fun deleteFriend(senderId: String, receiverId: String): Boolean
    fun isFriend(firstUser: String, secondUser: String): Boolean
}