package allin.data.postgres.entities

import allin.model.Participation
import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.varchar

interface ParticipationEntity : Entity<ParticipationEntity> {
    companion object : Entity.Factory<ParticipationEntity>()

    var id: String
    var bet: BetEntity
    var username: String
    var answer: String
    var stake: Int

    fun toParticipation() =
        Participation(
            id = id,
            betId = bet.id,
            username = username,
            answer = answer,
            stake = stake
        )
}

object ParticipationsEntity : Table<ParticipationEntity>("participation") {
    val id = varchar("id").primaryKey()
    val betId = varchar("bet").references(BetsEntity) { it.bet }
    val username = varchar("username").bindTo { it.username }
    val answer = varchar("answer").bindTo { it.answer }
    val stake = int("stake").bindTo { it.stake }
}

val Database.participations get() = this.sequenceOf(ParticipationsEntity)