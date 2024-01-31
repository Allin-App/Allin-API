package allin.data

import allin.model.Bet
import allin.model.UpdatedBetData

interface BetDataSource {
    fun getAllBets(): List<Bet>
    fun getBetById(id: String): Bet?
    fun getBetsNotFinished(): List<Bet>
    fun addBet(bet: Bet)
    fun removeBet(id: String): Boolean
    fun updateBet(data: UpdatedBetData): Boolean
}