package allin.entities

import allin.database
import allin.utils.Execute
import org.ktorm.dsl.*
import org.ktorm.entity.Entity
import org.ktorm.schema.Table
import org.ktorm.schema.uuid
import org.ktorm.schema.varchar
import java.util.*


interface ResponseEntity : Entity<ResponseEntity> {
    val betId: UUID
    val response: String
}

object ResponsesEntity : Table<ResponseEntity>("response") {
    val id = uuid("id").primaryKey()
    val response = varchar("response").primaryKey()
    fun createResponseTable(){
        val request="CREATE TABLE IF NOT EXISTS response (id UUID,response VARCHAR(250),CONSTRAINT pk_response_id PRIMARY KEY (id,response));"
        database.Execute(request)
    }

    fun getResponse(idBet: UUID): MutableList<String> {
        return database.from(ResponsesEntity)
            .select(response)
            .where { id eq idBet }
            .map { it[response].toString() }
            .toMutableList()
    }

    fun addResponse(responses : MutableList<String>, idBet : UUID ) {
        responses.forEach {selected ->
            database.insert(ResponsesEntity) {
                set(it.id, idBet)
                set(it.response,selected)
            }
        }
    }
}
