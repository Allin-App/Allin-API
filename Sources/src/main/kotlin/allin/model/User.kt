package allin.model

import allin.dto.UserDTO
import kotlinx.serialization.Serializable

const val DEFAULT_COIN_AMOUNT = 500
const val DAILY_GIFT_MIN = 10
const val DAILY_GIFT_MAX = 150

@Serializable
data class User(
    val id: String,
    val username: String,
    val email: String,
    var password: String,
    var nbCoins: Int = DEFAULT_COIN_AMOUNT,
    var token: String? = null,
    var image: String? = null,
    var bestWin: Int?,
    var nbBets: Int,
    var nbFriends: Int,
)

{
    fun toDto(friendStatus: FriendStatus? = null) =
        UserDTO(
            id = id,
            username = username,
            email = email,
            nbCoins = nbCoins,
            token = token,
            friendStatus = friendStatus,
            image = image,
            bestWin = bestWin?:0,
            nbBets = nbBets,
            nbFriends=nbFriends
        )
}

@Serializable
data class UserRequest(
    val username: String,
    val email: String,
    var password: String
)

@Serializable
data class CheckUser(
    val login: String,
    val password: String
)

@Serializable
data class UserImage(
    val id: String,
    val image: ByteArray,
)
