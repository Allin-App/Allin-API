package allin.model

import allin.dto.UserDTO
import allin.routing.users
import kotlinx.serialization.Serializable

@Serializable
data class User(val username: String, val email: String, var password: String, var nbCoins: Int = 1000, var token: String? = null)
@Serializable
data class CheckUser(val login: String,val password: String)
