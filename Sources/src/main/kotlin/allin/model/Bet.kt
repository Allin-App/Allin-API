package allin.model

import allin.serializer.DateSerializer
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class Bet(val id: Int, val theme: String, val sentenceBet: String, @Serializable(DateSerializer::class) val endRegistration: Date, @Serializable(DateSerializer::class) val endBet : Date, val isPrivate : Boolean, val response : MutableList<String>)