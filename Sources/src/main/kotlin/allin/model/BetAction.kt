package allin.model

import allin.dto.UserDTO
data class BetAction(val id:Int, val coins: Int, val user: String, val bet: Int)
data class BetActionCompleted(val id:Int, val coins: Int, val user: UserDTO, val bet: Bet)
