package allin.data.postgres

import allin.data.ParticipationDataSource
import allin.entities.ParticipationsEntity
import allin.model.Participation
import org.ktorm.database.Database
import org.ktorm.dsl.*
import java.util.*

class PostgresParticipationDataSource(private val database: Database) : ParticipationDataSource {

    private fun QueryRowSet.toParticipation() =
        Participation(
            id = this[ParticipationsEntity.id].toString(),
            betId = this[ParticipationsEntity.betId].toString(),
            username = this[ParticipationsEntity.username].toString(),
            answer = this[ParticipationsEntity.answer].toString(),
            stake = this[ParticipationsEntity.stake] ?: 0,
        )

    private fun Query.mapToParticipation() = this.map { it.toParticipation() }

    override fun addParticipation(participation: Participation) {
        database.insert(ParticipationsEntity) {
            set(it.id, UUID.fromString(participation.id))
            set(it.betId, UUID.fromString(participation.betId))
            set(it.username, participation.username)
            set(it.answer, participation.answer)
            set(it.stake, participation.stake)
        }
    }

    override fun getParticipationFromBetId(betid: String): List<Participation> {
        return database.from(ParticipationsEntity)
            .select()
            .where { ParticipationsEntity.betId eq UUID.fromString(betid) }
            .mapToParticipation()
    }

    override fun getParticipationFromUserId(username: String, betid: String): List<Participation> {
        return database.from(ParticipationsEntity)
            .select()
            .where { (ParticipationsEntity.betId eq UUID.fromString(betid)) and (ParticipationsEntity.username eq username) }
            .mapToParticipation()
    }

    fun getParticipationEntity(): List<Participation> {
        return database.from(ParticipationsEntity).select().mapToParticipation()
    }

    override fun deleteParticipation(id: String): Boolean {
        return database.delete(ParticipationsEntity) {
            it.id eq UUID.fromString(id)
        } > 0
    }

}