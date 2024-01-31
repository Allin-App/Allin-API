package allin.data

import allin.dto.UserDTO
import allin.model.User

interface UserDataSource {
    fun getUserByUsername(username: String): Pair<UserDTO?, String?>
    fun addUser(user: User)
    fun deleteUser(username: String): Boolean
    fun modifyUserCoins(username: String, amount: Int)
    fun userExists(username: String, email: String): Boolean
}