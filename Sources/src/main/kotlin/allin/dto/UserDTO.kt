package allin.dto

import io.github.smiley4.ktorswaggerui.dsl.Example
import kotlinx.serialization.Serializable

@Serializable
data class UserDTO(
    @Example("cabb366c-5a47-4b0f-81e1-25a08fe2c2fe")
    val id: String,
    @Example("Steve")
    val username: String,
    @Example("stevemaroco@gmail.com")
    val email: String,
    @Example("16027")
    val nbCoins: Int,
    @Example("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhdWQiOiJodHRwOi8vMC4wLjAuMDo4MDgwLyIsImlzcyI6Imh0dHA6Ly8wLjAuMC4wOjgwODAvIiwidXNlcm5hbWUiOiJ0ZXN0IiwiZXhwIjoxNzA3OTIyNjY1fQ.TwaT9Rd4Xkhg3l4fHiba0IEqnM7xUGJVFRrr5oaWOwQ")
    var token: String?
)
