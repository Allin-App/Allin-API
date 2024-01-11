package allin.entities

import org.ktorm.schema.Table
import org.ktorm.schema.double
import org.ktorm.schema.int
import org.ktorm.schema.varchar
object UserEntity : Table<Nothing>("utilisateur") {
    val id = int("id").primaryKey()
    val username = varchar("username")
    val password = varchar("password")
    val nbCoins = double("nbCoins")
}