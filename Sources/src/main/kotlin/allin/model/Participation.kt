package allin.model

import kotlinx.serialization.Serializable

@Serializable
data class Participation(
    val id: String,
    val betId: String,
    val userId: String,
    val answer: String,
    val stake: Int,
    val username: String,
    val imageUser: String? = null
)

@Serializable
data class ParticipationRequest(
    val betId: String,
    val answer: String,
    val stake: Int
)