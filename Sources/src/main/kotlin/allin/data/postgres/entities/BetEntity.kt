package allin.data.postgres.entities

import allin.model.*
import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.entity.*
import org.ktorm.schema.*
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime


interface BetEntity : Entity<BetEntity> {
    companion object : Entity.Factory<BetEntity>()

    var id: String
    var theme: String
    var sentenceBet: String
    var endRegistration: Instant
    var endBet: Instant
    var zoneId: String
    var isPrivate: Boolean
    var status: BetStatus
    var type: BetType
    var createdBy: String
    var popularityscore: Int

    fun toBet(database: Database): Bet {
        val betInfo = database.betInfos.find { it.id eq this.id }
        return Bet(
            id = id,
            theme = theme,
            sentenceBet = sentenceBet,
            status = status,
            type = type,
            endRegistration = ZonedDateTime.ofInstant(endRegistration, ZoneId.of(zoneId)),
            endBet = ZonedDateTime.ofInstant(endBet, ZoneId.of(zoneId)),
            isPrivate = isPrivate,
            response = if (type == BetType.BINARY) {
                listOf(YES_VALUE, NO_VALUE)
            } else {
                database.responses.filter { it.betId eq id }.map { it.response }
            },
            createdBy = database.users.first { it.id eq createdBy }.username,
            popularityscore = popularityscore,
            totalStakes = betInfo?.totalStakes ?: 0,
            totalParticipants = betInfo?.totalParticipants ?: 0
        )
    }

    fun toBetDetail(database: Database, userid: String): BetDetail {
        val bet = this.toBet(database)
        val participations = database.participations.filter { it.betId eq bet.id }
        val userParticipation = participations.find { it.userid eq userid }
        val participationEntities = participations.map { it.toParticipation(database) }

        val answerInfos = database.betAnswerInfos
            .filter { it.betId eq bet.id }
            .map { it.toBetAnswerInfo() }

        return BetDetail(
            bet = bet,
            answers = getBetAnswerDetail(bet, participationEntities, answerInfos),
            participations = participationEntities,
            userParticipation = userParticipation?.toParticipation(database),
            wonParticipation = if (bet.status == BetStatus.FINISHED) {
                val result = database.betResults.find { it.betId eq this.id }
                result?.let { r ->
                    participationEntities
                        .filter { it.answer == r.result }
                        .maxBy { it.stake }
                }
            } else null
        )
    }

}

object BetsEntity : Table<BetEntity>("bet") {
    val id = varchar("id").primaryKey().bindTo { it.id }
    val theme = varchar("theme").bindTo { it.theme }
    val sentenceBet = varchar("sentencebet").bindTo { it.sentenceBet }
    val endRegistration = timestamp("endregistration").bindTo { it.endRegistration }
    val endBet = timestamp("endbet").bindTo { it.endBet }
    val zoneId = varchar("zoneid").bindTo { it.zoneId }
    val isPrivate = boolean("isprivate").bindTo { it.isPrivate }
    val status = enum<BetStatus>("status").bindTo { it.status }
    val type = enum<BetType>("type").bindTo { it.type }
    val createdBy = varchar("createdby").bindTo { it.createdBy }
    val popularityscore = int("popularityscore").bindTo { it.popularityscore }
}

val Database.bets get() = this.sequenceOf(BetsEntity)