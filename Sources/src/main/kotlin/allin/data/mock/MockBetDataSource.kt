package allin.data.mock

import allin.data.BetDataSource
import allin.model.Bet
import allin.model.BetResult
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
            if (date >= bet.endRegistration) {
                if (date >= bet.endBet) {
                    bets[idx] = bet.copy(status = BetStatus.WAITING)
                } else {
                    bets[idx] = bet.copy(status = BetStatus.CLOSING)
                }
            }
        }
    }

    override fun getToConfirm(username: String): List<Bet> =
        bets.filter { it.createdBy == username && it.status == BetStatus.CLOSING }

    override fun confirmBet(betId: String, result: String) {
        results.add(
            BetResult(
                betId = betId,
                result = result
            )
        )
        bets.replaceAll {
            if (it.id == betId) {
                it.copy(status = BetStatus.FINISHED)
            } else it
        }
    }

    private val bets by lazy { mutableListOf<Bet>() }
    private val results by lazy { mutableListOf<BetResult>() }

}