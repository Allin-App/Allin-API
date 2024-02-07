package allin.data.mock

import allin.data.ParticipationDataSource
import allin.model.Participation

class MockParticipationDataSource(mockData: MockDataSource.MockData) : ParticipationDataSource {
    private val participations = mockData.participations

    override fun addParticipation(participation: Participation) {
        participations += participation
    }

    override fun getParticipationFromBetId(betid: String): List<Participation> =
        participations.filter { it.betId == betid }

    override fun getParticipationFromUserId(username: String, betid: String): List<Participation> =
        participations.filter { it.betId == betid && it.username == username }

    override fun deleteParticipation(id: String): Boolean =
        participations.removeIf { it.id == id }
}