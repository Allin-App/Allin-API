package allin.data.postgres.entities

import org.ktorm.entity.Entity
import org.ktorm.schema.Table
import org.ktorm.schema.varchar


const val YES_VALUE = "Yes"
const val NO_VALUE = "No"

interface ResponseEntity : Entity<ResponseEntity> {
    val betId: String
    val response: String
}

object ResponsesEntity : Table<ResponseEntity>("response") {
    val id = varchar("id").primaryKey()
    val response = varchar("response").primaryKey()
}
