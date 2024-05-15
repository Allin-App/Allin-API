package allin.data.postgres.entities

import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.Table
import org.ktorm.schema.varchar

interface ResponseEntity : Entity<ResponseEntity> {
    companion object : Entity.Factory<ResponseEntity>()

    var betId: String
    var response: String
}

object ResponsesEntity : Table<ResponseEntity>("response") {
    val betId = varchar("betid").primaryKey().bindTo { it.betId }
    val response = varchar("response").primaryKey().bindTo { it.response }
}

val Database.responses get() = this.sequenceOf(ResponsesEntity)
