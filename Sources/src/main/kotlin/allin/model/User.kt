package allin.model

import io.github.smiley4.ktorswaggerui.dsl.Example
import kotlinx.serialization.Serializable
import kotlin.random.Random

@Serializable
data class User(
    @Example("cabb366c-5a47-4b0f-81e1-25a08fe2c2fe")
    val id: String,
    @Example("Steve")
    val username: String,
    @Example("stevemaroco@gmail.com")
    val email: String,
    @Example("MarocoSteveHDOIU978*")
    var password: String,
    @Example("16027")
    var nbCoins: Int = 500,
    @Example("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOiJodHRwOi8vMC4wLjAuMDo4MDgwLyIsImlzcyI6Imh0dHA6Ly8wLjAuMC4wOjgwODAvIiwidXNlcm5hbWUiOiJ0ZXN0IiwiZXhwIjoxNzA3OTIyNjY1fQ.TwaT9Rd4Xkhg3l4fHiba0IEqnM7xUGJVFRrr5oaWOwQ")
    var token: String? = null
)

@Serializable
data class UserRequest(
    @Example("Steve")
    val username: String,
    @Example("stevemaroco@gmail.com")
    val email: String,
    @Example("MarocoSteveHDOIU978*")
    var password: String
)

@Serializable
data class CheckUser(
    @Example("stevemaroco@gmail.com")
    val login: String,
    @Example("MarocoSteveHDOIU978*")
    val password: String
)

fun getDailyGift() : Int{
    return Random.nextInt(10,150)
}
