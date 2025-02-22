package allin.model

import allin.model.BetStatus.IN_PROGRESS
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
    val status: BetStatus = IN_PROGRESS,
    val type: BetType,
    @Serializable(ZonedDateTimeSerializer::class) val endRegistration: ZonedDateTime,
    @Serializable(ZonedDateTimeSerializer::class) var endBet: ZonedDateTime,
    var isPrivate: Boolean,
    var response: List<String>,
    val createdBy: String = "",
    var popularityscore: Int = 0,
    val totalStakes: Int = 0,
    val totalParticipants: Int = 0,
    val userInvited: List<String>? = null
)

@Serializable
data class UpdatedBetData(
    val id: String,
    @Serializable(ZonedDateTimeSerializer::class) val endBet: ZonedDateTime,
    val isPrivate: Boolean,
    val response: List<String>
)

@Serializable
data class InvitationBet(
    val betid: String,
    val userId: String
)

@Serializable
data class UpdatedPrivateBet(
    val betid: String,
    val usersInvited: List<String>? = null
)