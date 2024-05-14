package allin.data

interface FriendDataSource {
    fun addFriend(sender: String, receiver: String)
    fun getFriendFromUserId(id: String)
    fun getFriendFromUsername(username: String)
    fun deleteFriend(senderId: String, receiverId: String)
    fun isFriend(firstUser: String, secondUser: String)
}