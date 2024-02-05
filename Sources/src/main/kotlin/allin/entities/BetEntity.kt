package allin.entities

import allin.model.BetStatus
import allin.model.BetType
import org.ktorm.entity.Entity
import org.ktorm.schema.*
import org.ktorm.support.postgresql.pgEnum
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
    val id = uuid("id").primaryKey()
    val theme = varchar("theme")
    val sentenceBet = varchar("sentencebet")
    val endRegistration = timestamp("endregistration")
    val endBet = timestamp("endbet")
    val isPrivate = boolean("isprivate")
    val status = pgEnum<BetStatus>("status").bindTo { it.status }
    val type = pgEnum<BetType>("type").bindTo { it.type }
    val createdBy = varchar("createdby")
}