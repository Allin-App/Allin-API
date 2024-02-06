package allin.data.postgres

import allin.data.BetDataSource
import allin.data.postgres.entities.*
import allin.data.postgres.entities.ResponsesEntity.response
import allin.model.Bet
import allin.model.BetStatus
import allin.model.BetType
import allin.model.UpdatedBetData
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
            status = this[BetsEntity.status] ?: BetStatus.IN_PROGRESS,
            type = this[BetsEntity.type] ?: BetType.CUSTOM,
            createdBy = this[BetsEntity.createdBy].toString(),
            response = let {
                val idBet = this[BetsEntity.id].toString()
                val type = this[BetsEntity.type] ?: BetType.CUSTOM
                if (type == BetType.CUSTOM) {
                    database.from(ResponsesEntity)
                        .select(response)
                        .where { ResponsesEntity.id eq UUID.fromString(idBet) }
                        .map { it[response].toString() }
                } else {
                    listOf(YES_VALUE, NO_VALUE)
                }
            }
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

    override fun getToConfirm(username: String): List<Bet> {
        return database.from(BetsEntity)
            .select()
            .where {
                (BetsEntity.createdBy eq username) and
                        (BetsEntity.status eq BetStatus.CLOSING)
            }.mapToBet()
    }

    override fun confirmBet(betId: String, result: String) {
        database.insert(BetResultsEntity) {
            set(it.betId, betId)
            set(it.result, result)
        }

        database.update(BetsEntity) {
            where { BetsEntity.id eq UUID.fromString(betId) }
            set(BetsEntity.status, BetStatus.FINISHED)
        }
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
            set(it.status, bet.status)
            set(it.type, bet.type)
        }

        if (bet.type == BetType.CUSTOM) {
            bet.response.forEach { selected ->
                database.insert(ResponsesEntity) {
                    set(it.id, UUID.fromString(bet.id))
                    set(it.response, selected)
                }
            }
        }
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

    override fun updateBetStatuses(date: ZonedDateTime) {
        database.update(BetsEntity) {
            set(BetsEntity.status, BetStatus.WAITING)
            where {
                (date.toInstant() greaterEq BetsEntity.endRegistration) and
                        (date.toInstant() less BetsEntity.endBet)
            }
        }

        database.update(BetsEntity) {
            set(BetsEntity.status, BetStatus.CLOSING)
            where {
                (date.toInstant() greaterEq BetsEntity.endRegistration) and
                        (date.toInstant() greaterEq BetsEntity.endBet)
            }
        }
    }
}