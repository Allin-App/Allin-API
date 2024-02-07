package allin.model

import kotlinx.serialization.Serializable

@Serializable
data class BetResultDetail(
    val betResult: BetResult,
    val bet: Bet,
    val participation: Participation,
    val amount: Int,
    val won: Boolean
)