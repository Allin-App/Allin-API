package allin.data.postgres.entities

import org.ktorm.entity.Entity
import org.ktorm.schema.Table
import org.ktorm.schema.uuid
import org.ktorm.schema.varchar
import java.util.*


interface BetResultEntity : Entity<BetResultEntity> {
    val bet: BetEntity
    val result: String
}

object BetResultsEntity : Table<BetResultEntity>("betresult") {
    val betId = uuid("betid").primaryKey().references(BetsEntity) { it.bet }
    val result = varchar("result").bindTo { it.result }
}

interface BetResultNotificationEntity : Entity<BetResultNotificationEntity> {
    val betId: UUID
    val username: String
}

object BetResultNotificationsEntity : Table<BetResultNotificationEntity>("betresult") {
    val betId = uuid("betid").primaryKey()
    val username = varchar("username").primaryKey()
}