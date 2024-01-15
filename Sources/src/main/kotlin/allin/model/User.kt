package allin.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val username: String,
    val email: String,
    var password: String,
    var nbCoins: Int = 500,
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
