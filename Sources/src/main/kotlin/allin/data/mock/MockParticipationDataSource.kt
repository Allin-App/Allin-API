package allin.data.mock

import allin.data.ParticipationDataSource
import allin.model.Participation

class MockParticipationDataSource : ParticipationDataSource {
    override fun addParticipation(participation: Participation) {
        participations += participations
    }

    override fun getParticipationFromBetId(betid: String): List<Participation> =
        participations.filter { it.betId == betid }

    override fun getParticipationFromUserId(username: String, betid: String): List<Participation> =
        participations.filter { it.betId == betid && it.username == username }

    override fun deleteParticipation(id: String): Boolean =
        participations.removeIf { it.id == id }

    private val participations by lazy { mutableListOf<Participation>() }

}