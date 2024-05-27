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
                users
                    .find { usr -> it.receiver == usr.id }
                    ?.toDto(friendStatus = FriendStatus.FRIEND)
            }

    override fun deleteFriend(senderId: String, receiverId: String) =
        friends.removeIf { (it.sender == senderId) && (it.receiver == receiverId) }


    override fun isFriend(firstUser: String, secondUser: String) =
        friends
            .filter { (it.sender == firstUser) and (it.receiver == secondUser) }
            .map { Friend(sender = it.sender, receiver = it.receiver) }
            .isNotEmpty()


    override fun filterUsersByUsername(fromUserId: String, search: String): List<UserDTO> =
        users.filter { (it.username.contains(search, ignoreCase = true)) }
            .map { user ->
                user.toDto(
                    friendStatus = friends.filter { friend ->
                        friend.sender == fromUserId && friend.receiver == user.id
                    }.let {
                        if (it.isEmpty()) FriendStatus.NOT_FRIEND
                        else friends.filter { friend ->
                            friend.sender == user.id && friend.receiver == fromUserId
                        }.let {
                            if (it.isEmpty()) FriendStatus.REQUESTED
                            else FriendStatus.FRIEND
                        }
                    }
                )
            }
}