package allin.data.mock

import allin.data.FriendDataSource
import allin.dto.UserDTO
import allin.model.Friend
import allin.model.FriendStatus

class MockFriendDataSource(private val mockData: MockDataSource.MockData) : FriendDataSource {

    private val friends get() = mockData.friends
    private val users get() = mockData.users

    override fun addFriend(sender: String, receiver: String) {
        mockData.friends.add(Friend(sender, receiver))
    }

    override fun getFriendFromUserId(id: String) =
        friends.map { Friend(sender = it.sender, receiver = it.receiver) }
            .filter { it.sender == id }
            .mapNotNull {
                users.find { usr -> it.receiver == usr.id }
                    ?.toDto(
                        friendStatus = if (isFriend(it.receiver, id)) {
                            FriendStatus.FRIEND
                        } else FriendStatus.REQUESTED
                    )
            }

    override fun getFriendRequestsFromUserId(id: String): List<UserDTO> {
        return friends
            .filter { (it.receiver == id) && !isFriend(id, it.sender) }
            .mapNotNull {
                users.find { usr -> usr.id == it.sender }
                    ?.toDto(friendStatus = FriendStatus.NOT_FRIEND)
            }
    }

    override fun deleteFriend(senderId: String, receiverId: String) =
        friends.removeIf { (it.sender == senderId) && (it.receiver == receiverId) }


    override fun isFriend(firstUser: String, secondUser: String) =
        friends.any { (it.sender == firstUser) and (it.receiver == secondUser) }


    override fun filterUsersByUsername(fromUserId: String, search: String): List<UserDTO> =
        users.filter { (it.username.contains(search, ignoreCase = true)) }
            .map { user ->
                user.toDto(friendStatus = getFriendStatus(fromUserId, user.id))
            }
}