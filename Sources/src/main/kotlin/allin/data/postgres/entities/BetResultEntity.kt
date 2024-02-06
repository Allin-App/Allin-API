package allin.data.postgres.entities

import org.ktorm.entity.Entity
import org.ktorm.schema.Table
import org.ktorm.schema.uuid
import org.ktorm.schema.varchar


interface BetResultEntity : Entity<BetResultEntity> {
    val bet: BetEntity
    val result: String
}

object BetResultsEntity : Table<BetResultEntity>("betresult") {
    val betId = uuid("betid").primaryKey().references(BetsEntity) { it.bet }
    val result = varchar("result").bindTo { it.result }
}