package allin.data.mock

import allin.data.BetDataSource
import allin.dto.UserDTO
import allin.model.*
import allin.model.BetStatus.*
import java.time.ZonedDateTime
import kotlin.math.roundToInt

class MockBetDataSource(private val mockData: MockDataSource.MockData) : BetDataSource {
    private val bets get() = mockData.bets
    private val results get() = mockData.results
    private val users get() = mockData.users
    private val participations get() = mockData.participations
    private val resultNotifications get() = mockData.resultNotifications
    private val betInfos get() = mockData.betInfos
    private val answerInfos get() = mockData.answerInfos

    override fun getAllBets(filters: List<BetFilter>, userDTO: UserDTO): List<Bet> {
        return when {
            filters.isEmpty() -> bets

            filters.size == 1 -> {
                val filter = filters[0]

                when (filter) {
                    BetFilter.PUBLIC -> bets.filter { !it.isPrivate }
                    BetFilter.INVITATION -> bets.filter { it.isPrivate }
                    BetFilter.FINISHED -> bets.filter { it.status == FINISHED }
                    BetFilter.IN_PROGRESS -> bets.filter {
                        it.status in listOf(IN_PROGRESS, WAITING, CLOSING)
                    }
                }.map { it }
            }

            else -> {
                bets.filter { bet ->
                    val public = (BetFilter.PUBLIC in filters) && !bet.isPrivate
                    val invitation = (BetFilter.INVITATION in filters) && bet.isPrivate
                    val finished =
                        (BetFilter.FINISHED in filters) and ((bet.status == FINISHED) or (bet.status == CANCELLED))
                    val inProgress = (BetFilter.IN_PROGRESS in filters) and (bet.status in listOf(
                        IN_PROGRESS,
                        WAITING,
                        CLOSING
                    ))

                    (public || invitation) && (finished or inProgress)
                }.map { it }
            }
        }
    }

    override fun getBetById(id: String): Bet? =
        bets.find { it.id == id }

    override fun getBetDetailById(id: String, username: String): BetDetail? =
        bets.find { it.id == id }?.toBetDetail(username)

    override fun removeBet(id: String): Boolean {
        betInfos.removeIf { it.id == id }
        answerInfos.removeIf { it.betId == id }
        return bets.removeIf { it.id == id }
    }

    override fun updateBet(data: UpdatedBetData): Boolean {
        return bets.find { it.id == data.id }?.let {
            it.isPrivate = data.isPrivate
        } != null
    }

    override fun getBetsNotFinished(): List<Bet> =
        bets.filter { it.endBet >= ZonedDateTime.now() }

    override fun addBet(bet: Bet) {
        bets += bet
        betInfos += BetInfo(id = bet.id, totalStakes = 0, totalParticipants = 0)
        bet.response.forEach {
            answerInfos += BetAnswerInfo(
                betId = bet.id,
                response = it,
                totalStakes = 0,
                odds = 1f
            )
        }
    }

    override fun updateBetStatuses(date: ZonedDateTime) {
        bets.forEachIndexed { idx, bet ->
            if (bet.status != CANCELLED && bet.status != FINISHED) {
                if (date >= bet.endRegistration) {
                    if (date >= bet.endBet) {
                        if (date.plusWeeks(1) >= bet.endBet) {
                            bets[idx] = bet.copy(status = CANCELLED)

                            participations
                                .filter { it.betId == bets[idx].id }
                                .forEach { p ->
                                    users.replaceAll {
                                        if (it.username == p.username) {
                                            it.copy(nbCoins = it.nbCoins + p.stake)
                                        } else it
                                    }
                                }
                        } else {
                            bets[idx] = bet.copy(status = CLOSING)
                        }
                    } else {
                        bets[idx] = bet.copy(status = WAITING)
                    }
                }
            }
        }
    }

    override fun getToConfirm(username: String): List<BetDetail> =
        bets.filter { it.createdBy == username && it.status == CLOSING }
            .map { it.toBetDetail(username) }

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
        val resultAnswerInfo = answerInfos.find { it.betId == betId && it.response == result }
        participations.filter { it.betId == betId && it.answer == result }
            .forEach { participation ->

                val amount = (participation.stake * (resultAnswerInfo?.odds ?: 1f)).roundToInt()
                users.replaceAll {
                    if (it.username == participation.username) {
                        it.copy(nbCoins = it.nbCoins + amount)
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
                val answerInfo = answerInfos.find { it.betId == bet.id && it.response == participation.answer }
                BetResultDetail(
                    betResult = result,
                    bet = bet,
                    participation = participation,
                    amount = (participation.stake * (answerInfo?.odds ?: 1f)).roundToInt(),
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

            val won = participation.answer == result.result
            val answerInfo = answerInfos.find {
                it.betId == bet.id && it.response == participation.answer
            }

            BetResultDetail(
                betResult = result,
                bet = bet,
                participation = participation,
                amount = if (won) {
                    (participation.stake * (answerInfo?.odds ?: 1f)).roundToInt()
                } else participation.stake,
                won = won
            )
        }.mapNotNull { it }
    }

    override fun getCurrent(username: String): List<BetDetail> {
        return bets.mapNotNull { bet ->
            when (bet.status) {
                CANCELLED, FINISHED -> return@mapNotNull null
                else -> {
                    val userParticipation = participations.find { it.username == username && it.betId == bet.id }
                    if (userParticipation == null) return@mapNotNull null
                    return@mapNotNull bet.toBetDetail(username)
                }
            }
        }
    }

    private fun Bet.toBetDetail(username: String): BetDetail {
        val participation = participations.find { it.username == username && it.betId == this.id }
        val participations = participations.filter { it.betId == this.id }

        return BetDetail(
            bet = this,
            answers = getBetAnswerDetail(
                bet = this,
                participations = participations,
                infos = answerInfos.filter { it.betId == this.id }
            ),
            participations = participations,
            userParticipation = participation,
            wonParticipation = if (this.status == FINISHED) {
                val result = results.find { it.betId == this.id }
                result?.let { r ->
                    participations
                        .filter { it.answer == r.result }
                        .maxBy { it.stake }
                }
            } else null
        )
    }

    override fun getMostPopularBet() =
        mockData.bets.filter { !it.isPrivate && it.status == WAITING }.maxBy { it.popularityscore }

    override fun updatePopularityScore(betId: String) {
        val bet = mockData.bets.firstOrNull { it.id == betId } ?: return
        val participations = mockData.participations.filter { it.betId == betId }
        val score = participations.size * participations.size + participations.sumOf { it.stake }
        bet.popularityscore = score
    }

    override fun addPrivateBet(bet: Bet) {
        TODO()
    }

    override fun isInvited(betid: String, userId: String): Boolean {
        TODO("Not yet implemented")
    }

}
