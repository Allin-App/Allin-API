package allin.data.postgres.entities

import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.Table
import org.ktorm.schema.varchar

interface BetResultNotificationEntity : Entity<BetResultNotificationEntity> {
    companion object : Entity.Factory<BetResultNotificationEntity>()

    var betId: String
    var username: String
}

object BetResultNotificationsEntity : Table<BetResultNotificationEntity>("betresultnotification") {
    val betId = varchar("betid").primaryKey()
    val username = varchar("username").primaryKey()
}

val Database.betResultNotifications get() = this.sequenceOf(BetResultNotificationsEntity)