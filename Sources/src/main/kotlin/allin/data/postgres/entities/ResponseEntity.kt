package allin.data.postgres.entities

import org.ktorm.entity.Entity
import org.ktorm.schema.Table
import org.ktorm.schema.uuid
import org.ktorm.schema.varchar
import java.util.*


const val YES_VALUE = "Yes"
const val NO_VALUE = "No"

interface ResponseEntity : Entity<ResponseEntity> {
    val betId: UUID
    val response: String
}

object ResponsesEntity : Table<ResponseEntity>("response") {
    val id = uuid("id").primaryKey()
    val response = varchar("response").primaryKey()
}
