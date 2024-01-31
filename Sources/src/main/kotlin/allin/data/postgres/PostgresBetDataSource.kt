package allin.data.postgres

import allin.data.BetDataSource
import allin.entities.BetsEntity
import allin.model.Bet
import allin.model.UpdatedBetData
import allin.utils.Execute
import org.ktorm.database.Database
import org.ktorm.dsl.*
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.*

class PostgresBetDataSource(private val database: Database) : BetDataSource {

    private fun QueryRowSet.toBet() =
        Bet(
            id = this[BetsEntity.id].toString(),
            theme = this[BetsEntity.theme].toString(),
            sentenceBet = this[BetsEntity.sentenceBet].toString(),
            endRegistration = this[BetsEntity.endRegistration]!!.atZone(ZoneId.of("Europe/Paris")),
            endBet = this[BetsEntity.endBet]!!.atZone(ZoneId.of("Europe/Paris")),
            isPrivate = this[BetsEntity.isPrivate] ?: false,
            response = mutableListOf(), // ResponsesEntity.getResponse(UUID.fromString(this[BetsEntity.id].toString())),
            createdBy = this[BetsEntity.createdBy].toString()
        )

    private fun Query.mapToBet() = this.map { it.toBet() }

    override fun getAllBets(): List<Bet> =
        database.from(BetsEntity).select().mapToBet()

    override fun getBetById(id: String): Bet? =
        database.from(BetsEntity).select().where {
            BetsEntity.id eq UUID.fromString(id)
        }.mapToBet().firstOrNull()

    override fun getBetsNotFinished(): List<Bet> {
        val currentTime = ZonedDateTime.now(ZoneId.of("Europe/Paris"))
        return database.from(BetsEntity)
            .select()
            .where { BetsEntity.endBet greaterEq currentTime.toInstant() }
            .mapToBet()
    }

    override fun addBet(bet: Bet) {
        database.insert(BetsEntity) {
            set(it.id, UUID.fromString(bet.id))
            set(it.endBet, bet.endBet.toInstant())
            set(it.endRegistration, bet.endRegistration.toInstant())
            set(it.sentenceBet, bet.sentenceBet)
            set(it.theme, bet.theme)
            set(it.isPrivate, bet.isPrivate)
            set(it.createdBy, bet.createdBy)
        }
        // ResponsesEntity.addResponse(bet.response, UUID.fromString(bet.id))
    }

    override fun removeBet(id: String): Boolean {
        return database.delete(BetsEntity) { it.id eq UUID.fromString(id) } > 0
    }

    override fun updateBet(data: UpdatedBetData): Boolean {
        return database.update(BetsEntity) {
            set(BetsEntity.isPrivate, data.isPrivate)
            where { BetsEntity.id eq UUID.fromString(data.id) }
        } > 0
    }

    fun createBetsTable() {
        val request =
            "CREATE TABLE IF not exists bet ( id uuid PRIMARY KEY, theme VARCHAR(255), endregistration timestamp,endbet timestamp,sentencebet varchar(500),isprivate boolean, createdby varchar(250))"
        database.Execute(request)
    }
}