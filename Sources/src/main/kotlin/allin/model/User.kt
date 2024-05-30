package allin.model

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
    var token: String? = null
)

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
