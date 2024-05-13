package allin.data.postgres.entities

import allin.model.BetStatus
import allin.model.BetType
import org.ktorm.entity.Entity
import org.ktorm.schema.*
import java.time.ZonedDateTime


interface BetEntity : Entity<BetEntity> {
    val theme: String
    val sentenceBet: String
    val endRegistration: ZonedDateTime
    val endBet: ZonedDateTime
    val isPrivate: Boolean
    val status: BetStatus
    val type: BetType
    val createdBy: String
}

object BetsEntity : Table<BetEntity>("bet") {
    val id = varchar("id").primaryKey()
    val theme = varchar("theme").bindTo { it.theme }
    val sentenceBet = varchar("sentencebet").bindTo { it.sentenceBet }
    val endRegistration = timestamp("endregistration")
    val endBet = timestamp("endbet")
    val isPrivate = boolean("isprivate").bindTo { it.isPrivate }
    val status = enum<BetStatus>("status").bindTo { it.status }
    val type = enum<BetType>("type").bindTo { it.type }
    val createdBy = varchar("createdby").bindTo { it.createdBy }
}