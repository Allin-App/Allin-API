package allin.data.postgres

import allin.data.BetDataSource
import allin.data.postgres.entities.*
import allin.model.*
import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.entity.*
import java.time.ZoneId
import java.time.ZonedDateTime

class PostgresBetDataSource(private val database: Database) : BetDataSource {

    override fun getAllBets(): List<Bet> =
        database.bets.map { it.toBet(database) }

    override fun getBetById(id: String): Bet? =
        database.bets.find { it.id eq id }?.toBet(database)

    override fun getBetDetailById(id: String, username: String): BetDetail? =
        database.bets.find { it.id eq id }?.toBetDetail(database, username)

    override fun getBetsNotFinished(): List<Bet> {
        val currentTime = ZonedDateTime.now(ZoneId.of("Europe/Paris"))
        return database.bets
            .filter { it.endBet greaterEq currentTime.toInstant() }
            .map { it.toBet(database) }
    }

    override fun getToConfirm(username: String): List<BetDetail> {
        return database.bets
            .filter {
                (it.createdBy eq username) and (BetsEntity.status eq BetStatus.CLOSING)
            }
            .map { it.toBetDetail(database, username) }
    }

    override fun confirmBet(betId: String, result: String) {
        database.bets.find { it.id eq betId }?.let { bet ->
            bet.status = BetStatus.FINISHED
            bet.flushChanges()

            database.betResults.add(
                BetResultEntity {
                    this.bet = bet
                    this.result = result
                }
            )
        }

        database.participations.filter {
            (ParticipationsEntity.betId eq betId) and
                    (ParticipationsEntity.answer eq result)
        }.forEach {
            database.betResultNotifications.add(
                BetResultNotificationEntity {
                    this.betId = betId
                    this.username = it.username
                }
            )
        }
    }

    override fun getWonNotifications(username: String): List<BetResultDetail> {
        return database.betResultNotifications
            .filter { it.username eq username }
            .flatMap { notif ->
                notif.delete()

                database.participations
                    .filter {
                        (it.username eq username) and
                                (it.betId eq notif.betId)
                    }
                    .mapNotNull { participation ->
                        database.betResults
                            .find { it.betId eq participation.bet.id }
                            ?.toBetResultDetail(
                                database,
                                participation
                            )
                    }

            }
    }

    override fun getHistory(username: String): List<BetResultDetail> {
        return database.participations
            .filter { it.username eq username }
            .mapNotNull { participation ->
                database.betResults
                    .find { it.betId eq participation.bet.id }
                    ?.toBetResultDetail(
                        database,
                        participation
                    )
            }
    }

    override fun getCurrent(username: String): List<BetDetail> {
        return database.participations
            .filter { it.username eq username }
            .mapNotNull {
                if (it.bet.status !in listOf(BetStatus.FINISHED, BetStatus.CANCELLED)) {
                    it.bet.toBetDetail(
                        database = database,
                        username = username
                    )
                } else null
            }
    }

    override fun getMostPopularBet(): Bet? {

        database.bets.forEach {
            println(it)
        }

        val max=database.bets.filter { (it.isPrivate eq false) and (it.status eq BetStatus.WAITING) }.maxBy { it.popularityscore }
        println(max)
        if(max!=null){
            return database.bets.filter { (it.popularityscore eq max) and (it.isPrivate eq false) and (it.status eq BetStatus.WAITING) }.map { it.toBet(database) }.first()
        }
        return null
    }

    override fun addBet(bet: Bet) {
        database.bets.add(
            BetEntity {
                this.id = bet.id
                this.endBet = bet.endBet.toInstant()
                this.endRegistration = bet.endRegistration.toInstant()
                this.zoneId = bet.endBet.zone.id
                this.sentenceBet = bet.sentenceBet
                this.theme = bet.theme
                this.isPrivate = bet.isPrivate
                this.createdBy = bet.createdBy
                this.status = bet.status
                this.type = bet.type
            }
        )

        if (bet.type == BetType.CUSTOM) {
            bet.response.forEach { selected ->
                database.responses.add(
                    ResponseEntity {
                        this.betId = bet.id
                        this.response = selected
                    }
                )
            }
        }
    }

    override fun removeBet(id: String): Boolean {
        database.betInfos.removeIf { it.id eq id }
        database.betAnswerInfos.removeIf { it.betId eq id }
        return database.bets.removeIf { it.id eq id } > 0
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