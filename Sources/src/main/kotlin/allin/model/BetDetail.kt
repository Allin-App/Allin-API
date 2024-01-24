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

fun getBetAnswerDetail(bet: Bet, participations: List<Participation>): List<BetAnswerDetail> {
    return bet.response.map { response ->
        val responseParticipations = participations.filter { it.answer == response }
        BetAnswerDetail(
            response = response,
            totalStakes = responseParticipations.sumOf { it.stake },
            totalParticipants = responseParticipations.size,
            highestStake = responseParticipations.maxOfOrNull { it.stake } ?: 0,
            odds = if (participations.isEmpty()) 1f else responseParticipations.size / participations.size.toFloat()
        )
    }

}
