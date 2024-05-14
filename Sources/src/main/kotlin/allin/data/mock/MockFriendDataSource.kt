package allin.data.mock

import allin.data.FriendDataSource
import allin.model.User

class MockFriendDataSource(mockData: MockDataSource.MockData) : FriendDataSource {
    override fun addFriend(sender: String, receiver: String) {
        TODO("Not yet implemented")
    }

    override fun getFriendFromUserId(id: String): List<User> {
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