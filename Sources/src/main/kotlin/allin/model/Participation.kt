package allin.model

import kotlinx.serialization.Serializable

@Serializable
data class Participation(
    val id: String,
    val betId: String,
    val username: String,
    val answer: String,
    val stake: Int
)

@Serializable
data class ParticipationRequest(
    val betId: String,
    val answer: String,
    val stake: Int
)