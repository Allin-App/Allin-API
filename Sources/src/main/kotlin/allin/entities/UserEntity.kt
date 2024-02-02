package allin.entities

import allin.database
import allin.dto.UserDTO
import allin.model.User
import allin.utils.Execute
import io.ktor.util.date.*
import org.h2.util.DateTimeUtils.currentTimestamp
import org.ktorm.dsl.*
import org.ktorm.entity.*
import org.ktorm.schema.*
import java.time.Duration
import java.util.*
import java.util.UUID.fromString

interface UserEntity : Entity<UserEntity> {
    val username: String
    var email: String
    var password: String
    var nbCoins: Double
}
object UsersEntity : Table<UserEntity>("utilisateur") {
    val id = uuid("id").primaryKey()
    val username = varchar("username")
    val password = varchar("password")
    val nbCoins = double("coins")
    val email = varchar("email")
    val lastGift = varchar("lastgift")


    fun getUserToUserDTO(): MutableList<UserDTO> {
        return database.from(UsersEntity).select().map {
            row -> UserDTO(
                row[id].toString(),
                row[username].toString(),
                row[email].toString(),
                row[nbCoins]?:0.0,
                null
            )
        }.toMutableList()
    }

    fun createUserTable(){
        val request="CREATE TABLE IF not exists utilisateur ( id uuid PRIMARY KEY, username VARCHAR(255), password VARCHAR(255),coins double precision,email VARCHAR(255), lastgift timestamp)"
        database.Execute(request)
    }


    fun getUserByUsernameAndPassword(login: String): Pair<UserDTO?, String?> {
        return database.from(UsersEntity)
            .select()
            .where { (username eq login) /*and (password eq passwordParam)*/ }
            .map { row ->
                Pair(
                    UserDTO(
                        row[id].toString(),
                        row[username].toString(),
                        row[email].toString(),
                        row[nbCoins] ?: 0.0,
                        null
                    ),
                    row[password].toString()
                )
            }
            .firstOrNull() ?: Pair(null, null)
    }

    fun addUserEntity(user : User){
        database.insert(UsersEntity){
            set(it.id,fromString(user.id))
            set(it.nbCoins,user.nbCoins)
            set(it.username,user.username)
            set(it.password,user.password)
            set(it.email,user.email)
        }
    }
    fun deleteUserByUsername(username: String): Boolean {
        val deletedCount = database.delete(UsersEntity) {
            it.username eq username
        }
        return deletedCount > 0
    }

    fun canHaveDailyGift(username: String): Boolean {
        val request = "SELECT CASE WHEN lastgift IS NULL THEN TRUE ELSE lastgift < current_timestamp - interval '1 day' END AS can_have_daily_gift, " +
                "CASE WHEN lastgift IS NULL THEN null ELSE current_timestamp - lastgift END AS time_remaining " +
                "FROM utilisateur WHERE username = '$username';"
        val returnCode= database.Execute(request)

        if(returnCode?.next().toString()=="true"){
            return true
        }
        return false
    }
}


