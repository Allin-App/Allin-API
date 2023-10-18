package allin.dto

import allin.model.User
import kotlinx.serialization.Serializable

@Serializable
data class UserDTO(val username: String,val email: String, val nbCoins: Int)
@Serializable
data class UserDTOWithToken(val username: String,val email: String, val nbCoins: Int, val token:String?)
fun convertUserToUserDTO(user: User): UserDTO {
    return UserDTO(user.username, user.email, user.nbCoins)
}
fun convertUserToUserDTOToken(user: User): UserDTOWithToken {
    return UserDTOWithToken(user.username, user.email, user.nbCoins,user.token)
}
