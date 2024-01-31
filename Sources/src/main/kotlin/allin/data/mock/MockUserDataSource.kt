package allin.data.mock

import allin.data.UserDataSource
import allin.dto.UserDTO
import allin.model.User

class MockUserDataSource : UserDataSource {
    override fun getUserByUsername(username: String): Pair<UserDTO?, String?> =
        users.find { it.username == username }?.let {
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
        users.removeIf { it.username == username }

    override fun modifyUserCoins(username: String, amount: Int) {
        users.find { it.username == username }?.let {
            it.nbCoins += amount
        }
    }

    override fun userExists(username: String, email: String): Boolean =
        users.any { it.username == username && it.email == email }

    private val users by lazy {
        mutableListOf<User>()
    }
}