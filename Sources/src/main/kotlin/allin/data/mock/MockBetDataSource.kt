package allin.data.mock

import allin.data.BetDataSource
import allin.model.*
import allin.model.BetStatus.*
import java.time.ZonedDateTime

class MockBetDataSource(mockData: MockDataSource.MockData) : BetDataSource {
    private val bets = mockData.bets
    private val results = mockData.results
    private val users = mockData.users
    private val participations = mockData.participations
    private val resultNotifications = mockData.resultNotifications

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
            if (bet.status != CANCELLED && bet.status != FINISHED) {
                if (date >= bet.endRegistration) {
                    if (date >= bet.endBet) {
                        bets[idx] = bet.copy(status = WAITING)
                    } else {
                        bets[idx] = bet.copy(status = CLOSING)
                    }
                }
            }
        }
    }

    override fun getToConfirm(username: String): List<Bet> =
        bets.filter { it.createdBy == username && it.status == CLOSING }

    override fun confirmBet(betId: String, result: String) {
        results.add(
            BetResult(
                betId = betId,
                result = result
            )
        )
        bets.replaceAll {
            if (it.id == betId) {
                it.copy(status = FINISHED)
            } else it
        }

        participations.filter { it.betId == betId && it.answer == result }
            .forEach { participation ->
                users.replaceAll {
                    if (it.username == participation.username) {
                        it.copy(nbCoins = it.nbCoins + participation.stake)
                    } else it
                }
                resultNotifications.add(Pair(betId, participation.username))
            }
    }

    override fun getWonNotifications(username: String): List<BetResultDetail> {
        return bets.map { bet ->
            val notification = resultNotifications.find { it.first == bet.id } ?: return@map null
            val result = results.find { it.betId == bet.id } ?: return@map null
            val participation = participations.find { it.username == username && it.betId == bet.id }
                ?: return@map null

            if (participation.answer == result.result) {
                resultNotifications.remove(notification)
                BetResultDetail(
                    betResult = result,
                    bet = bet,
                    participation = participation,
                    amount = participation.stake,
                    won = true
                )
            } else null
        }.mapNotNull { it }
    }

    override fun getHistory(username: String): List<BetResultDetail> {
        return bets.map { bet ->
            val result = results.find { it.betId == bet.id } ?: return@map null
            val participation = participations.find { it.username == username && it.betId == bet.id }
                ?: return@map null

            BetResultDetail(
                betResult = result,
                bet = bet,
                participation = participation,
                amount = participation.stake,
                won = participation.answer == result.result
            )
        }.mapNotNull { it }
    }

    override fun getCurrent(username: String): List<BetDetail> {
        return bets.map { bet ->
            when (bet.status) {
                CANCELLED, FINISHED -> return@map null
                else -> {
                    val participation = participations.find { it.username == username && it.betId == bet.id }
                        ?: return@map null

                    val participations = participations.filter { it.betId == bet.id }


                    BetDetail(
                        bet = bet,
                        answers = getBetAnswerDetail(bet, participations),
                        participations = participations,
                        userParticipation = participation
                    )
                }
            }
        }.mapNotNull { it }
    }
}
