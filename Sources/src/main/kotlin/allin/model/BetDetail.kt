package allin.model

import kotlinx.serialization.Serializable

@Serializable
data class BetAnswerDetail(
    val response: String, // La réponse (ex: "Yes", "No" etc...)
    val totalStakes: Int, // Le nombre total d'argent misé sur cette réponse
    val totalParticipants: Int, // Le nombre total de participant
    val highestStake: Int, // Plus grosse mise
    val odds: Float // Cote du bet
)

@Serializable
data class BetDetail(
    val bet: Bet, // Le Bet
    val answers: List<BetAnswerDetail>?, // Pour chaque réponse possible du bet les détails
    val participations: List<Participation>?, // La liste des participations
    val userParticipation: Participation? // La participation du User current
)

fun getBetAnswerDetail(participations: List<Participation>): List<BetAnswerDetail> {
    val groupedParticipations = participations.groupBy { it.answer }
    val betAnswerDetails = mutableListOf<BetAnswerDetail>()
    for ((answer, participationList) in groupedParticipations) {
        val totalStakes = participationList.sumBy { it.stake }
        val totalParticipants = participationList.size
        val highestStake = participationList.maxByOrNull { it.stake }?.stake ?: 0
        val odds = 1.0f
        val betAnswerDetail = BetAnswerDetail(answer, totalStakes, totalParticipants, highestStake, odds)
        betAnswerDetails.add(betAnswerDetail)
    }
    return betAnswerDetails
}
