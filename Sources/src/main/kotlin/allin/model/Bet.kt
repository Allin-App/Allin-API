package allin.model

import allin.dto.UserDTOWithToken
import allin.serializer.DateSerializer
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class Bet(val id: Int, val theme: String, val sentenceBet: String, @Serializable(DateSerializer::class) val endRegistration: Date, @Serializable(DateSerializer::class) var endBet : Date, var isPrivate : Boolean, var response : MutableList<String>, val createdBy : String)

@Serializable
data class UpdatedBetData(val id: Int,@Serializable(DateSerializer::class) val endBet: Date, val isPrivate: Boolean, val response: MutableList<String>)

@Serializable
data class BetWithoutId(val theme: String, val sentenceBet: String, @Serializable(DateSerializer::class) val endRegistration: Date, @Serializable(DateSerializer::class) var endBet : Date, var isPrivate : Boolean, var response : MutableList<String>, val createdBy : String)

fun convertBetWithoutIdToBet(betWithoutId: BetWithoutId,id : Int, username : String): Bet {
    return Bet(id,betWithoutId.theme,betWithoutId.sentenceBet,betWithoutId.endRegistration, betWithoutId.endBet, betWithoutId.isPrivate, betWithoutId.response, username)
}
