package allin.data.postgres

import allin.data.BetDataSource
import allin.data.postgres.entities.*
import allin.dto.UserDTO
import allin.model.*
import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.entity.*
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

class PostgresBetDataSource(private val database: Database) : BetDataSource {

    override fun getAllBets(filters: List<BetFilter>, userDTO: UserDTO): List<Bet> {
        return when {
            filters.isEmpty() -> database.bets.map { it.toBet(database) }
                .filter { (!it.isPrivate) or (isInvited(it.id, userDTO.id)) or (it.createdBy == userDTO.id) }

            filters.size == 1 -> {
                val filter = filters.first()

                when (filter) {
                    BetFilter.PUBLIC -> database.bets.filter { !it.isPrivate }
                    BetFilter.INVITATION -> database.bets.filter { it.isPrivate }
                    BetFilter.FINISHED -> database.bets.filter { it.status eq BetStatus.FINISHED }
                    BetFilter.IN_PROGRESS -> database.bets.filter {
                        it.status inList listOf(BetStatus.IN_PROGRESS, BetStatus.WAITING, BetStatus.CLOSING)
                    }
                }.map { it.toBet(database) }
                    .filter { (!it.isPrivate) or (isInvited(it.id, userDTO.id)) or (it.createdBy == userDTO.id) }
            }

            else -> {
                database.bets.filter { bet ->
                    val public = (BetFilter.PUBLIC in filters) and !bet.isPrivate
                    val invitation = (BetFilter.INVITATION in filters) and bet.isPrivate
                    val finished =
                        (BetFilter.FINISHED in filters) and ((bet.status eq BetStatus.FINISHED) or (bet.status eq BetStatus.CANCELLED))
                    val inProgress = (BetFilter.IN_PROGRESS in filters) and (bet.status inList listOf(
                        BetStatus.IN_PROGRESS,
                        BetStatus.WAITING,
                        BetStatus.CLOSING
                    ))

                    (public or invitation) and (finished or inProgress)
                }.map { it.toBet(database) }
                    .filter { (!it.isPrivate) or (isInvited(it.id, userDTO.id)) or (it.createdBy == userDTO.id) }
            }
        }
    }

    override fun getBetById(id: String): Bet? =
        database.bets.find { it.id eq id }?.toBet(database)

    override fun getBetDetailById(id: String, username: String): BetDetail? =
        database.bets.find { it.id eq id }?.toBetDetail(database, username)

    override fun getBetsNotFinished(): List<Bet> {
        val currentTime = ZonedDateTime.now(ZoneId.of("+02:00"))
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

        val resultAnswerInfo = database.betAnswerInfos
            .find { (it.betId eq betId) and (it.response eq result) }
            ?.toBetAnswerInfo()

        database.participations.filter {
            (ParticipationsEntity.betId eq betId) and
                    (ParticipationsEntity.answer eq result)
        }.forEach { participation ->
            database.betResultNotifications.add(
                BetResultNotificationEntity {
                    this.betId = betId
                    this.username = participation.username
                }
            )

            val amount = (participation.stake * (resultAnswerInfo?.odds ?: 1f)).roundToInt()
            database.update(UsersEntity) { usr ->
                set(usr.nbCoins, usr.nbCoins + amount)
                where { usr.username eq participation.username }
            }
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
        return database.bets
            .filter { (it.isPrivate eq false) and (it.status eq BetStatus.IN_PROGRESS) }
            .sortedByDescending { it.popularityscore }
            .firstOrNull()
            ?.toBet(database)
    }

    override fun updatePopularityScore(betId: String) {
        database.bets.filter { it.id eq betId }.firstOrNull() ?: return
        val participations = database.participations.filter { it.betId eq betId }
        val score = (participations.count() * participations.count()) + participations.map { it.stake }.sum()
        database.update(BetsEntity) {
            set(it.popularityscore, score)
            where { it.id eq betId }
        }
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

        database.betInfos.add(
            BetInfoEntity {
                this.id = bet.id
                this.totalStakes = 0
                this.totalParticipants = 0
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
        database.bets
            .filter {
                (date.toInstant() greaterEq BetsEntity.endRegistration) and
                        (BetsEntity.status notEq BetStatus.FINISHED) and
                        (BetsEntity.status notEq BetStatus.CANCELLED)
            }.let {
                it.filter { date.toInstant() less BetsEntity.endBet }.forEach { bet ->
                    bet.status = BetStatus.WAITING
                    bet.flushChanges()
                }

                it.filter { date.toInstant() greaterEq BetsEntity.endBet }.forEach { bet ->
                    if (date.toInstant() >= bet.endBet.plus(7, ChronoUnit.DAYS)) {
                        database.participations
                            .filter { it.betId eq bet.id }
                            .forEach { participation ->
                                database.users.find { it.username eq participation.username }?.let { user ->
                                    user.nbCoins += participation.stake
                                    user.flushChanges()
                                }
                            }
                        bet.status = BetStatus.CANCELLED
                        bet.flushChanges()
                    } else {
                        bet.status = BetStatus.CLOSING
                        bet.flushChanges()
                    }
                }
            }
    }

    override fun addPrivateBet(bet: Bet) {
        addBet(bet)
        bet.userInvited?.forEach {
            database.privatebets.add(PrivateBetEntity {
                betId = bet.id
                userId = it
            })
        }
    }

    override fun isInvited(betid: String, userId: String): Boolean {
        return database.privatebets.filter { (it.betid eq betid) and (it.userId eq userId) }.isNotEmpty()
    }
}