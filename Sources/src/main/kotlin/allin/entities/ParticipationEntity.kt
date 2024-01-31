package allin.entities

import org.ktorm.entity.Entity
import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.uuid
import org.ktorm.schema.varchar

interface ParticipationEntity : Entity<ParticipationEntity> {
    val id: String
    val betId: String
    val username: String
    val answer: String
    val stake: Int
}


object ParticipationsEntity : Table<BetEntity>("participation") {
    val id = uuid("id").primaryKey()
    val betId = uuid("bet")
    val username = varchar("username")
    val answer = varchar("answer")
    val stake = int("stake")
}