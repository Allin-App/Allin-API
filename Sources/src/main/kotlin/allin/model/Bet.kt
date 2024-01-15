package allin.model

import allin.serializer.ZonedDateTimeSerializer
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class Bet(val id: Int, val theme: String, val sentenceBet: String, @Serializable(ZonedDateTimeSerializer::class) val endRegistration: Date, @Serializable(ZonedDateTimeSerializer::class) var endBet : Date, var isPrivate : Boolean, var response : MutableList<String>, var createdBy : String)
@Serializable
data class UpdatedBetData(val id: Int,@Serializable(ZonedDateTimeSerializer::class) val endBet: Date, val isPrivate: Boolean, val response: MutableList<String>)