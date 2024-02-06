package allin.model

import kotlinx.serialization.Serializable

@Serializable
data class ParticipationDetail(
    val id: String,
    val bet: Bet,
    val username: String,
    val answer: String,
    val stake: Int
)