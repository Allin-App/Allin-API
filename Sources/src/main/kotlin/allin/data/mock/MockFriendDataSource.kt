package allin.data.mock

import allin.data.FriendDataSource

class MockFriendDataSource(mockData: MockDataSource.MockData) : FriendDataSource {
    override fun addFriend(sender: String, receiver: String) {
        TODO("Not yet implemented")
    }

    override fun getFriendFromUserId(id: String): List<String> {
        TODO("Not yet implemented")
    }

    override fun deleteFriend(senderId: String, receiverId: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun isFriend(firstUser: String, secondUser: String): Boolean {
        TODO("Not yet implemented")
    }

}