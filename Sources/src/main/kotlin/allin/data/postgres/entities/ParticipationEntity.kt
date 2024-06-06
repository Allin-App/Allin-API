package allin.data.postgres.entities

import allin.model.Participation
import allin.utils.AppConfig
import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.entity.Entity
import org.ktorm.entity.filter
import org.ktorm.entity.map
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.varchar

interface ParticipationEntity : Entity<ParticipationEntity> {
    companion object : Entity.Factory<ParticipationEntity>()

    var id: String
    var bet: BetEntity
    var userid: String
    var answer: String
    var stake: Int

    fun toParticipation(database: Database) =
        Participation(
            id = id,
            betId = bet.id,
            userId = userid,
            answer = answer,
            stake = stake,
            username = database.users.filter { it.id eq userid }.map { it.username }.first,
            imageUser = AppConfig.imageManager.getImage(id, database)
        )
}

object ParticipationsEntity : Table<ParticipationEntity>("participation") {
    val id = varchar("id").primaryKey().bindTo { it.id }
    val betId = varchar("bet").references(BetsEntity) { it.bet }
    val userid = varchar("userid").bindTo { it.userid }
    val answer = varchar("answer").bindTo { it.answer }
    val stake = int("stake").bindTo { it.stake }
}

val Database.participations get() = this.sequenceOf(ParticipationsEntity)