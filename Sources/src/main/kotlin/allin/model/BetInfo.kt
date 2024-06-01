package allin.model

import kotlinx.serialization.Serializable

@Serializable
data class BetInfo(
    var id: String,
    var totalStakes: Int,
    var totalParticipants: Int
)

@Serializable
data class BetAnswerInfo(
    val betId: String,
    val response: String,
    val totalStakes: Int,
    val odds: Float
)
