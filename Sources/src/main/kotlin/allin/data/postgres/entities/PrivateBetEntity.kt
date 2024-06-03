package allin.data.postgres.entities

import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.Table
import org.ktorm.schema.varchar

interface PrivateBetEntity : Entity<PrivateBetEntity> {
    companion object : Entity.Factory<PrivateBetEntity>()

    var betId: String
    var userId: String
}

object PrivateBetsEntity : Table<PrivateBetEntity>("privatebet") {
    val betid = varchar("betid").bindTo { it.betId }
    val userId = varchar("userid").bindTo { it.userId }
}

val Database.privatebets get() = this.sequenceOf(PrivateBetsEntity)