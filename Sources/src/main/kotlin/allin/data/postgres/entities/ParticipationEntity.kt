package allin.data.postgres.entities

import org.ktorm.entity.Entity
import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.varchar

interface ParticipationEntity : Entity<ParticipationEntity> {
    val id: String
    val bet: BetEntity
    val username: String
    val answer: String
    val stake: Int
}


object ParticipationsEntity : Table<ParticipationEntity>("participation") {
    val id = varchar("id").primaryKey()
    val betId = varchar("bet").references(BetsEntity) { it.bet }
    val username = varchar("username")
    val answer = varchar("answer")
    val stake = int("stake")
}
