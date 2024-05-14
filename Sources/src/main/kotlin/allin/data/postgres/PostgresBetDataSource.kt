package allin.data.postgres

import allin.data.BetDataSource
import allin.data.postgres.entities.*
import allin.data.postgres.entities.ResponsesEntity.response
import allin.model.*
import org.ktorm.database.Database
import org.ktorm.dsl.*
import java.time.ZoneId
import java.time.ZonedDateTime

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
                        .where { ResponsesEntity.id eq idBet }
                        .map { it[response].toString() }
                } else {
                    listOf(YES_VALUE, NO_VALUE)
                }
            }
        )

    private fun QueryRowSet.toParticipation() =
        Participation(
            id = this[ParticipationsEntity.id]?.toString() ?: "",
            betId = this[ParticipationsEntity.betId]?.toString() ?: "",
            username = this[ParticipationsEntity.username] ?: "",
            answer = this[ParticipationsEntity.answer] ?: "",
            stake = this[ParticipationsEntity.stake] ?: 0
        )

    private fun QueryRowSet.toBetResultDetail() =
        BetResultDetail(
            betResult = BetResult(
                betId = this[BetResultsEntity.betId]?.toString() ?: "",
                result = this[BetResultsEntity.result] ?: ""
            ),
            bet = this.toBet(),
            participation = this.toParticipation(),
            amount = this[ParticipationsEntity.stake] ?: 0,
            won = this[ParticipationsEntity.answer] == this[BetResultsEntity.result]
        )

    private fun Query.mapToBet() = this.map { it.toBet() }
    private fun Query.mapToBetResultDetail() = this.map { it.toBetResultDetail() }

    override fun getAllBets(): List<Bet> =
        database.from(BetsEntity).select().mapToBet()

    override fun getBetById(id: String): Bet? =
        database.from(BetsEntity).select().where {
            BetsEntity.id eq id
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
            where { BetsEntity.id eq betId }
            set(BetsEntity.status, BetStatus.FINISHED)
        }

        database.from(ParticipationsEntity)
            .select()
            .where {
                (ParticipationsEntity.betId eq betId) and
                        (ParticipationsEntity.answer eq result)
            }
            .forEach { participation ->
                database.insert(BetResultNotificationsEntity) {
                    set(it.betId, betId)
                    set(it.username, participation[ParticipationsEntity.username])
                }
            }
    }

    override fun getWonNotifications(username: String): List<BetResultDetail> {
        return database.from(BetsEntity)
            .innerJoin(ParticipationsEntity, on = BetsEntity.id eq ParticipationsEntity.betId)
            .innerJoin(BetResultsEntity, on = BetsEntity.id eq BetResultsEntity.betId)
            .innerJoin(BetResultNotificationsEntity, on = BetsEntity.id eq BetResultNotificationsEntity.betId)
            .select()
            .where {
                (BetResultsEntity.result eq ParticipationsEntity.answer) and
                        (ParticipationsEntity.username eq username)
            }.let {
                it.forEach { row ->
                    row[BetsEntity.id]?.let { betId ->
                        database.delete(BetResultNotificationsEntity) {
                            (it.betId eq betId) and (it.username eq username)
                        }
                    }
                }
                it
            }.mapToBetResultDetail()
    }

    override fun getHistory(username: String): List<BetResultDetail> {
        return database.from(BetsEntity)
            .innerJoin(ParticipationsEntity, on = BetsEntity.id eq ParticipationsEntity.betId)
            .innerJoin(BetResultsEntity, on = BetsEntity.id eq BetResultsEntity.betId)
            .select()
            .where { ParticipationsEntity.username eq username }.mapToBetResultDetail()
    }

    override fun getCurrent(username: String): List<BetDetail> {
        return database.from(BetsEntity)
            .innerJoin(ParticipationsEntity, on = BetsEntity.id eq ParticipationsEntity.betId)
            .select()
            .where {
                (BetsEntity.status notEq BetStatus.FINISHED) and
                        (BetsEntity.status notEq BetStatus.CANCELLED) and
                        (ParticipationsEntity.username eq username)
            }.map {
                val participations = it[BetsEntity.id]?.let { betId ->
                    database.from(ParticipationsEntity)
                        .select().where { ParticipationsEntity.betId eq betId }.map { it.toParticipation() }
                } ?: emptyList()

                val bet = it.toBet()
                BetDetail(
                    bet = bet,
                    answers = getBetAnswerDetail(bet, participations),
                    participations = participations,
                    userParticipation = it.toParticipation()

                )
            }
    }

    override fun addBet(bet: Bet) {
        database.insert(BetsEntity) {
            set(it.id, bet.id)
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
                    set(it.id, bet.id)
                    set(it.response, selected)
                }
            }
        }
    }

    override fun removeBet(id: String): Boolean {
        return database.delete(BetsEntity) { it.id eq id } > 0
    }

    override fun updateBet(data: UpdatedBetData): Boolean {
        return database.update(BetsEntity) {
            set(BetsEntity.isPrivate, data.isPrivate)
            where { BetsEntity.id eq data.id }
        } > 0
    }

    override fun updateBetStatuses(date: ZonedDateTime) {
        database.update(BetsEntity) {
            set(BetsEntity.status, BetStatus.IN_PROGRESS)
            where {
                (date.toInstant() greaterEq BetsEntity.endRegistration) and
                        (date.toInstant() less BetsEntity.endBet) and
                        (BetsEntity.status notEq BetStatus.FINISHED) and
                        (BetsEntity.status notEq BetStatus.CANCELLED)
            }
        }

        database.update(BetsEntity) {
            set(BetsEntity.status, BetStatus.CLOSING)
            where {
                (date.toInstant() greaterEq BetsEntity.endRegistration) and
                        (date.toInstant() greaterEq BetsEntity.endBet) and
                        (BetsEntity.status notEq BetStatus.FINISHED) and
                        (BetsEntity.status notEq BetStatus.CANCELLED)
            }
        }
    }
}