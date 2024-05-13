package allin.data.postgres

import allin.data.ParticipationDataSource
import allin.data.postgres.entities.ParticipationsEntity
import allin.model.Participation
import org.ktorm.database.Database
import org.ktorm.dsl.*

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
            set(it.id, participation.id)
            set(it.betId, participation.betId)
            set(it.username, participation.username)
            set(it.answer, participation.answer)
            set(it.stake, participation.stake)
        }
    }

    override fun getParticipationFromBetId(betid: String): List<Participation> =
        database.from(ParticipationsEntity)
            .select()
            .where { ParticipationsEntity.betId eq betid }
            .mapToParticipation()

    override fun getParticipationFromUserId(username: String, betid: String): List<Participation> =
        database.from(ParticipationsEntity)
            .select()
            .where { (ParticipationsEntity.betId eq betid) and (ParticipationsEntity.username eq username) }
            .mapToParticipation()

    fun getParticipationEntity(): List<Participation> =
        database.from(ParticipationsEntity).select().mapToParticipation()

    override fun deleteParticipation(id: String): Boolean =
        database.delete(ParticipationsEntity) {
            it.id eq id
        } > 0
}