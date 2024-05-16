package allin.data.postgres.entities

import allin.model.BetResult
import allin.model.BetResultDetail
import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.Table
import org.ktorm.schema.varchar


interface BetResultEntity : Entity<BetResultEntity> {
    companion object : Entity.Factory<BetResultEntity>()

    var bet: BetEntity
    var result: String

    fun toBetResult() =
        BetResult(
            betId = bet.id,
            result = result
        )

    fun toBetResultDetail(
        database: Database,
        participationEntity: ParticipationEntity
    ) =
        BetResultDetail(
            betResult = this.toBetResult(),
            bet = bet.toBet(database),
            participation = participationEntity.toParticipation(),
            amount = participationEntity.stake,
            won = participationEntity.answer == result
        )
}

object BetResultsEntity : Table<BetResultEntity>("betresult") {
    val betId = varchar("betid").primaryKey().references(BetsEntity) { it.bet }
    val result = varchar("result").bindTo { it.result }
}

val Database.betResults get() = this.sequenceOf(BetResultsEntity)