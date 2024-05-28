package allin.model

import allin.model.BetStatus.WAITING
import allin.serializer.ZonedDateTimeSerializer
import kotlinx.serialization.Serializable
import java.time.ZonedDateTime

const val YES_VALUE = "Yes"
const val NO_VALUE = "No"

@Serializable
data class Bet(
    val id: String = "",
    val theme: String,
    val sentenceBet: String,
    val status: BetStatus = WAITING,
    val type: BetType,
    @Serializable(ZonedDateTimeSerializer::class) val endRegistration: ZonedDateTime,
    @Serializable(ZonedDateTimeSerializer::class) var endBet: ZonedDateTime,
    var isPrivate: Boolean,
    var response: List<String>,
    val createdBy: String = "",
    var popularityscore: Int = 0
)

@Serializable
data class UpdatedBetData(
    val id: String,
    @Serializable(ZonedDateTimeSerializer::class) val endBet: ZonedDateTime,
    val isPrivate: Boolean,
    val response: List<String>
)