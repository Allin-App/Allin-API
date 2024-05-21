package allin.data

import allin.model.*
import java.time.ZonedDateTime

interface BetDataSource {
    fun getAllBets(filters: List<BetFilter>): List<Bet>
    fun getBetById(id: String): Bet?
    fun getBetDetailById(id: String, username: String): BetDetail?
    fun getBetsNotFinished(): List<Bet>
    fun addBet(bet: Bet)
    fun removeBet(id: String): Boolean
    fun updateBet(data: UpdatedBetData): Boolean
    fun updateBetStatuses(date: ZonedDateTime)
    fun getToConfirm(username: String): List<BetDetail>
    fun confirmBet(betId: String, result: String)
    fun getWonNotifications(username: String): List<BetResultDetail>
    fun getHistory(username: String): List<BetResultDetail>
    fun getCurrent(username: String): List<BetDetail>
}