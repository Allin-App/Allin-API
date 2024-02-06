package allin.model

import kotlinx.serialization.Serializable

@Serializable
data class AllInResponse<T : Any>(
    val value: T,
    val toConfirm: List<Bet>,
    val won: List<BetResult>
)