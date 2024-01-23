package allin.entities

import allin.database
import allin.entities.ResponsesEntity.getResponse
import allin.model.Bet
import allin.utils.Execute
import org.ktorm.dsl.*
import org.ktorm.entity.Entity
import org.ktorm.schema.*
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.UUID.fromString


interface BetEntity : Entity<BetEntity> {
    val theme: String
    val sentenceBet: String
    val endRegistration: ZonedDateTime
    val endBet: ZonedDateTime
    val isPrivate: Boolean
    val createdBy: String
}

object BetsEntity : Table<BetEntity>("bet") {
    val id = uuid("id").primaryKey()
    val theme = varchar("theme")
    val sentenceBet = varchar("sentencebet")
    val endRegistration = timestamp("endregistration")
    val endBet = timestamp("endbet")
    val isPrivate = boolean("isprivate")
    val createdBy = varchar("createdby")

    fun getBets(): MutableList<Bet> {
        return database.from(BetsEntity).select().map {
            row -> Bet(
            row[id].toString(),
            row[theme].toString(),
            row[sentenceBet].toString(),
            row[endRegistration]!!.atZone(ZoneId.of("Europe/Paris")),
            row[endBet]!!.atZone(ZoneId.of("Europe/Paris")),
            row[isPrivate]?: false,
            getResponse(fromString(row[id].toString())),
            row[createdBy].toString()
        )
        }.toMutableList()
    }

    fun createBetsTable(){
        val request="CREATE TABLE IF not exists bet ( id uuid PRIMARY KEY, theme VARCHAR(255), endregistration timestamp,endbet timestamp,sentencebet varchar(500),isprivate boolean, createdby varchar(250))"
        database.Execute(request)
    }

    fun addBetEntity(bet : Bet) {
        database.insert(BetsEntity) {
            set(it.id, fromString(bet.id))
            set(it.endBet,bet.endBet.toInstant())
            set(it.endRegistration,bet.endRegistration.toInstant())
            set(it.sentenceBet,bet.sentenceBet)
            set(it.theme, bet.theme)
            set(it.isPrivate, bet.isPrivate)
            set(it.createdBy, bet.createdBy)
        }
        ResponsesEntity.addResponse(bet.response,fromString(bet.id))
    }
}