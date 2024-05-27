package allin.dto

import allin.model.FriendStatus
import kotlinx.serialization.Serializable

@Serializable
data class UserDTO(
    val id: String,
    val username: String,
    val email: String,
    val nbCoins: Int,
    var token: String?,
    val friendStatus: FriendStatus?
)
