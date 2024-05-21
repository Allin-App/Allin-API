package allin.model

import kotlinx.serialization.Serializable

enum class BetFilter {
    PUBLIC,
    INVITATION,
    IN_PROGRESS,
    FINISHED
}

@Serializable
data class BetFiltersRequest(
    val filters: List<BetFilter>
)