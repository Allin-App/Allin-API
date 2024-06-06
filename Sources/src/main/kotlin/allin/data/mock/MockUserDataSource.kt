package allin.data.mock

import allin.data.UserDataSource
import allin.dto.UserDTO
import allin.model.User
import java.time.ZonedDateTime

class MockUserDataSource(private val mockData: MockDataSource.MockData) : UserDataSource {
    private val users get() = mockData.users
    private val lastGifts get() = mockData.lastGifts

    override fun getUserByUsername(username: String): Pair<UserDTO?, String?> =
        users.find { (it.username == username) or (it.email == username) }?.let { usr ->
            Pair(
                UserDTO(
                    id = usr.id,
                    username = usr.username,
                    email = usr.email,
                    nbCoins = usr.nbCoins,
                    token = usr.token,
                    image = null,
                    nbBets = mockData.participations.count { it.userId == usr.id },
                    nbFriends = mockData.friends.count { f ->
                        f.receiver == usr.username &&
                                mockData.friends.any { it.sender == usr.username && it.receiver == f.sender }
                    },
                    bestWin = mockData.participations
                        .filter {
                            (it.id == usr.id) &&
                                    (mockData.results.find { r -> r.betId == it.betId })?.result == it.answer
                        }
                        .maxBy { it.stake }
                        .stake,
                    friendStatus = null,
                ),
                usr.password
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
        val user = users.find { it.id == userid }
        if (user != null) {
            user.image = image.toString()
        }
    }

    override fun removeImage(userid: String) {
        val user = users.find { it.id == userid }
        if (user != null) {
            user.image = null
        }
    }

    override fun getImage(userid: String) =
        users.find { it.id == userid }?.image


    override fun getUserById(id: String) =
        mockData.users.find { it.id == id }?.let { usr ->
            UserDTO(
                id = usr.id,
                username = usr.username,
                email = usr.email,
                nbCoins = usr.nbCoins,
                token = usr.token,
                image = null,
                nbBets = mockData.participations.count { it.userId == usr.id },
                nbFriends = mockData.friends.count { f ->
                    f.receiver == usr.username &&
                            mockData.friends.any { it.sender == usr.username && it.receiver == f.sender }
                },
                bestWin = mockData.participations
                    .filter {
                        (it.id == usr.id) &&
                                (mockData.results.find { r -> r.betId == it.betId })?.result == it.answer
                    }
                    .maxBy { it.stake }
                    .stake,
                friendStatus = null,
            )
        }
}