package allin.model

import kotlinx.serialization.Serializable

@Serializable
data class User(val username: String, val email: String, var password: String, var nbCoins: Double = 1000.0, var token: String? = null)
@Serializable
data class CheckUser(val login: String,val password: String)
