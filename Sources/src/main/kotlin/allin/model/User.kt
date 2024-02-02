package allin.model

import kotlinx.serialization.Serializable
import kotlin.random.Random

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

    fun getDailyGift() : Int{
        return Random.nextInt(10,150)
    }
