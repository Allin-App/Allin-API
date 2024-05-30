package allin.data.mock

import allin.data.UserDataSource
import allin.dto.UserDTO
import allin.model.User
import java.time.ZonedDateTime

class MockUserDataSource(private val mockData: MockDataSource.MockData) : UserDataSource {
    private val users get() = mockData.users
    private val lastGifts get() = mockData.lastGifts

    override fun getUserByUsername(username: String): Pair<UserDTO?, String?> =
        users.find { (it.username == username) or (it.email == username) }?.let {
            Pair(
                UserDTO(
                    id = it.id,
                    username = it.username,
                    email = it.email,
                    nbCoins = it.nbCoins,
                    token = it.token
                ),
                it.password
            )
        } ?: Pair(null, null)

    override fun addUser(user: User) {
        users += user
    }

    override fun deleteUser(username: String): Boolean =
        users.removeIf { (it.username == username) or (it.email == username) }

    override fun addCoins(username: String, amount: Int) {
        users.find { it.username == username }?.let {
            it.nbCoins += amount
        }
    }

    override fun removeCoins(username: String, amount: Int) {
        users.find { it.username == username }?.let {
            it.nbCoins -= amount
        }
    }

    override fun userExists(username: String) =
        users.any { it.username == username }

    override fun emailExists(email: String) =
        users.any { it.email == email }

    override fun canHaveDailyGift(username: String): Boolean {
        val value = lastGifts[username]?.let {
            it.plusDays(1) <= ZonedDateTime.now()
        } ?: true
        lastGifts[username] = ZonedDateTime.now()
        return value
    }

    override fun addImage(userid: String, image: ByteArray) {
        TODO("Not yet implemented")
    }

    override fun removeImage(userid: String) {
        TODO("Not yet implemented")
    }

    override fun getImage(userid: String): ByteArray? {
        TODO("Not yet implemented")
    }

}