package allin.data

import allin.model.Bet
import allin.model.UpdatedBetData
import java.time.ZonedDateTime

interface BetDataSource {
    fun getAllBets(): List<Bet>
    fun getBetById(id: String): Bet?
    fun getBetsNotFinished(): List<Bet>
    fun addBet(bet: Bet)
    fun removeBet(id: String): Boolean
    fun updateBet(data: UpdatedBetData): Boolean
    fun updateBetStatuses(date: ZonedDateTime)
    fun getToConfirm(username: String): List<Bet>
    fun confirmBet(betId: String, result: String)
}