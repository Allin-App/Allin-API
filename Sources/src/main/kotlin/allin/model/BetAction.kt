package allin.model

import allin.dto.UserDTOWithToken
data class BetAction(val id:Int, val coins: Int, val user: String, val bet: Int)
data class BetActionCompleted(val id:Int, val coins: Int, val user: UserDTOWithToken, val bet: Bet)
