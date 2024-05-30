package allin.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserDTO(
    val id: String,
    val username: String,
    val email: String,
    val nbCoins: Int,
    var token: String?,
    val image: String?
)