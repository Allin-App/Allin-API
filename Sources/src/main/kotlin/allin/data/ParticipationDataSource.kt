package allin.data

import allin.model.Participation

interface ParticipationDataSource {
    fun addParticipation(participation: Participation)
    fun getParticipationFromBetId(betid: String): List<Participation>
    fun deleteParticipation(id: String): Boolean
}