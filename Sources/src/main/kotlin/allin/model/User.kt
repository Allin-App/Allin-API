package allin.model

import kotlinx.serialization.Serializable

@Serializable
data class User(val username: String, val email: String, val password: String, var nbCoins: Int)

@Serializable
data class CheckUser(val username: String,val password: String)