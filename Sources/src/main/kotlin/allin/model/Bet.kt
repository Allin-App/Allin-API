package allin.model

import allin.serializer.UUIDSerializer
import allin.serializer.ZonedDateTimeSerializer
import kotlinx.serialization.Serializable
import java.time.ZonedDateTime
import java.util.*

@Serializable
data class Bet(
    val id: String = "",
    val theme: String,
    val sentenceBet: String,
    @Serializable(ZonedDateTimeSerializer::class) val endRegistration: ZonedDateTime,
    @Serializable(ZonedDateTimeSerializer::class) var endBet: ZonedDateTime,
    var isPrivate: Boolean,
    var response: MutableList<String>,
    val createdBy: String = ""
)

@Serializable
data class UpdatedBetData(
    val id: String,
    @Serializable(ZonedDateTimeSerializer::class) val endBet: ZonedDateTime,
    val isPrivate: Boolean,
    val response: MutableList<String>
)