package allin.model

import kotlinx.serialization.Serializable

@Serializable
data class BetInfo(
    val id: String,
    val totalStakes: Int,
)

@Serializable
data class BetAnswerInfo(
    val betId: String,
    val response: String,
    val totalStakes: Int,
    val odds: Float
)
