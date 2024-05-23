package allin.data.mock

import allin.data.FriendDataSource
import allin.model.Friend

class MockFriendDataSource(private val mockData: MockDataSource.MockData) : FriendDataSource {

    private val friends get() = mockData.friends
    private val users get() = mockData.users

    override fun addFriend(sender: String, receiver: String) {
        mockData.friends.add(Friend(sender, receiver))
    }

    override fun getFriendFromUserId(id: String) =
        friends.map { Friend(sender = it.sender, receiver = it.receiver) }
            .filter { it.sender == id }
            .mapNotNull { users.find { usr -> it.receiver == usr.id }?.toDto() }

    override fun deleteFriend(senderId: String, receiverId: String) =
        friends.removeIf { (it.sender == senderId) && (it.receiver == receiverId) }


    override fun isFriend(firstUser: String, secondUser: String) =
        friends
            .filter { (it.sender == firstUser) and (it.receiver == secondUser) }
            .map { Friend(sender = it.sender, receiver = it.receiver) }
            .isNotEmpty()
}