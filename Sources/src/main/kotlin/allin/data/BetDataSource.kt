package allin.data

import allin.dto.UserDTO
import allin.model.*
import java.time.ZonedDateTime

interface BetDataSource {
    fun getAllBets(filters: List<BetFilter>, userDTO: UserDTO): List<Bet>
    fun getBetById(id: String): Bet?
    fun getBetDetailById(id: String, userid: String): BetDetail?
    fun getBetsNotFinished(): List<Bet>
    fun addBet(bet: Bet)
    fun removeBet(id: String): Boolean
    fun updateBet(data: UpdatedBetData): Boolean
    fun updateBetStatuses(date: ZonedDateTime)
    fun getToConfirm(user: UserDTO): List<BetDetail>
    fun confirmBet(betId: String, result: String)
    fun getWonNotifications(userid: String): List<BetResultDetail>
    fun getHistory(userid: String): List<BetResultDetail>
    fun getCurrent(userid: String): List<BetDetail>
    fun getMostPopularBet(): Bet?
    fun updatePopularityScore(betId: String)
    fun addPrivateBet(bet: Bet)
    fun isInvited(betid: String, userId: String): Boolean
    fun addUserInPrivatebet(updatedPrivateBet: UpdatedPrivateBet)
}