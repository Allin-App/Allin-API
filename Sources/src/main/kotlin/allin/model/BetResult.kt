package allin.model

import kotlinx.serialization.Serializable

@Serializable
data class BetResult(
    val betId: String,
    val result: String
)