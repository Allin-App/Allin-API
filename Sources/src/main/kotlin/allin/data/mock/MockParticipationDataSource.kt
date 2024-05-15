package allin.data.mock

import allin.data.ParticipationDataSource
import allin.model.Participation

class MockParticipationDataSource(private val mockData: MockDataSource.MockData) : ParticipationDataSource {
    private val participations get() = mockData.participations
    private val betInfos get() = mockData.betInfos
    private val answerInfos get() = mockData.answerInfos

    override fun addParticipation(participation: Participation) {
        participations += participation
        var betTotalStakes = 0

        betInfos.replaceAll {
            if (participation.betId == it.id) {
                betTotalStakes = it.totalStakes + participation.stake
                it.copy(totalStakes = betTotalStakes)
            } else {
                it
            }
        }

        answerInfos.replaceAll {
            if (participation.betId == it.betId) {
                if (participation.answer == it.response) {
                    val answerTotalStakes = it.totalStakes + participation.stake
                    val probability = answerTotalStakes / betTotalStakes.toFloat()
                    it.copy(
                        totalStakes = answerTotalStakes,
                        odds = 1 / probability
                    )
                } else {
                    val probability = it.totalStakes / betTotalStakes.toFloat()
                    it.copy(odds = 1 / probability)
                }
            } else {
                it
            }
        }
    }

    override fun getParticipationFromBetId(betid: String): List<Participation> =
        participations.filter { it.betId == betid }

    override fun getParticipationFromUserId(username: String, betid: String): List<Participation> =
        participations.filter { it.betId == betid && it.username == username }

    override fun deleteParticipation(id: String): Boolean {
        val participation = participations.find { it.id == id }
        val result = participations.remove(participation)
        var betTotalStakes = 0

        betInfos.replaceAll {
            if (participation?.betId == it.id) {
                betTotalStakes = it.totalStakes - participation.stake
                it.copy(totalStakes = betTotalStakes)
            } else {
                it
            }
        }

        answerInfos.replaceAll {
            if (participation?.betId == it.betId) {
                if (participation.answer == it.response) {
                    val answerTotalStakes = it.totalStakes - participation.stake
                    val probability = answerTotalStakes / betTotalStakes.toFloat()
                    it.copy(
                        totalStakes = answerTotalStakes,
                        odds = 1 / probability
                    )
                } else {
                    val probability = it.totalStakes / betTotalStakes.toFloat()
                    it.copy(odds = 1 / probability)
                }
            } else {
                it
            }
        }

        return result
    }

}