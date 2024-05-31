package allin.data

import allin.dto.UserDTO
import allin.model.User

interface UserDataSource {
    fun getUserByUsername(username: String): Pair<UserDTO?, String?>
    fun addUser(user: User)
    fun deleteUser(username: String): Boolean
    fun addCoins(username: String, amount: Int)
    fun removeCoins(username: String, amount: Int)
    fun userExists(username: String): Boolean
    fun emailExists(email: String): Boolean
    fun canHaveDailyGift(username: String): Boolean
    fun addImage(userid: String, image: ByteArray)
    fun removeImage(userid: String)
    fun getImage(userid: String): String?
}