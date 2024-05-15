package allin.data.postgres.entities

import allin.model.BetAnswerInfo
import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.Table
import org.ktorm.schema.float
import org.ktorm.schema.int
import org.ktorm.schema.varchar

interface BetAnswerInfoEntity : Entity<BetAnswerInfoEntity> {
    companion object : Entity.Factory<BetAnswerInfoEntity>()

    var betId: String
    var response: String
    var totalStakes: Int
    var odds: Float

    fun toBetAnswerInfo() =
        BetAnswerInfo(
            betId = betId,
            response = response,
            totalStakes = totalStakes,
            odds = odds
        )
}

object BetAnswerInfosEntity : Table<BetAnswerInfoEntity>("betanswerinfo") {
    val betId = varchar("betid").primaryKey().bindTo { it.betId }
    val response = varchar("response").primaryKey().bindTo { it.response }
    val totalStakes = int("totalstakes").bindTo { it.totalStakes }
    val odds = float("odds").bindTo { it.odds }
}

val Database.betAnswerInfos get() = this.sequenceOf(BetAnswerInfosEntity)
