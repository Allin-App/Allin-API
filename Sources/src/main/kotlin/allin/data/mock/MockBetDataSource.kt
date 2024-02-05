package allin.data.mock

import allin.data.BetDataSource
import allin.model.Bet
import allin.model.BetStatus
import allin.model.UpdatedBetData
import java.time.ZonedDateTime

class MockBetDataSource : BetDataSource {
    override fun getAllBets(): List<Bet> = bets
    override fun getBetById(id: String): Bet? =
        bets.find { it.id == id }

    override fun removeBet(id: String): Boolean =
        bets.removeIf { it.id == id }

    override fun updateBet(data: UpdatedBetData): Boolean {
        return bets.find { it.id == data.id }?.let {
            it.isPrivate = data.isPrivate
        } != null
    }

    override fun getBetsNotFinished(): List<Bet> =
        bets.filter { it.endBet >= ZonedDateTime.now() }

    override fun addBet(bet: Bet) {
        bets += bet
    }

    override fun updateBetStatuses(date: ZonedDateTime) {
        bets.forEachIndexed { idx, bet ->
            if (bet.endRegistration >= date) {
                if (bet.endBet >= date) {
                    bets[idx] = bet.copy(status = BetStatus.WAITING)
                } else {
                    bets[idx] = bet.copy(status = BetStatus.CLOSING)
                }
            }
        }
    }

    private val bets by lazy { mutableListOf<Bet>() }

}