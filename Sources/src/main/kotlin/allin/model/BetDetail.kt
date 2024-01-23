package allin.model

data class BetAnswerDetail(
    val response: String, // La réponse (ex: "Yes", "No" etc...)
    val totalStakes: Int, // Le nombre total d'argent misé sur cette réponse
    val totalParticipants: Int, // Le nombre total de participant
    val highestStake: Int, // Plus grosse mise
    val odds: Float // Cote du bet
)

data class BetDetail(
    val bet: Bet, // Le Bet
    val answers: List<BetAnswerDetail>, // Pour chaque réponse possible du bet les détails
    val participations: List<Participation>, // La liste des participations
    val userParticipation: Participation? // La participation du User current
)