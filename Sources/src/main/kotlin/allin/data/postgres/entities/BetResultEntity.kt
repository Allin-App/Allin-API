package allin.data.postgres.entities

import org.ktorm.entity.Entity
import org.ktorm.schema.Table
import org.ktorm.schema.varchar


interface BetResultEntity : Entity<BetResultEntity> {
    val bet: BetEntity
    val result: String
}

object BetResultsEntity : Table<BetResultEntity>("betresult") {
    val betId = varchar("betid").primaryKey().references(BetsEntity) { it.bet }
    val result = varchar("result").bindTo { it.result }
}

interface BetResultNotificationEntity : Entity<BetResultNotificationEntity> {
    val betId: String
    val username: String
}

object BetResultNotificationsEntity : Table<BetResultNotificationEntity>("betresult") {
    val betId = varchar("betid").primaryKey()
    val username = varchar("username").primaryKey()
}