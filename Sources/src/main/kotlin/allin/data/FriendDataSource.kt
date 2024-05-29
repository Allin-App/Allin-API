package allin.data

import allin.dto.UserDTO
import allin.model.FriendStatus

interface FriendDataSource {
    fun addFriend(sender: String, receiver: String)
    fun getFriendFromUserId(id: String): List<UserDTO>
    fun getFriendRequestsFromUserId(id: String): List<UserDTO>
    fun deleteFriend(senderId: String, receiverId: String): Boolean
    fun isFriend(firstUser: String, secondUser: String): Boolean
    fun filterUsersByUsername(fromUserId: String, search: String): List<UserDTO>

    fun getFriendStatus(firstUser: String, secondUser: String) =
        if (isFriend(firstUser, secondUser)) {
            if (isFriend(secondUser, firstUser)) {
                FriendStatus.FRIEND
            } else FriendStatus.REQUESTED
        } else FriendStatus.NOT_FRIEND
}