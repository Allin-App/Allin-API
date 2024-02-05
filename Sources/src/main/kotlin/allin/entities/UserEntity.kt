package allin.entities

import allin.database
import allin.dto.UserDTO
import allin.model.User
import allin.utils.Execute
import allin.utils.ExecuteWithResult
import org.ktorm.database.use
import org.ktorm.dsl.*
import org.ktorm.entity.*
import org.ktorm.schema.*
import java.time.Instant.now
import java.util.*
import java.util.UUID.fromString

interface UserEntity : Entity<UserEntity> {
    val username: String
    var email: String
    var password: String
    var nbCoins: Int
}
object UsersEntity : Table<UserEntity>("utilisateur") {
    val id = uuid("id").primaryKey()
    val username = varchar("username")
    val password = varchar("password")
    val nbCoins = int("coins")
    val email = varchar("email")
    val lastGift = timestamp("lastgift")

    fun getUserToUserDTO(): MutableList<UserDTO> {
        return database.from(UsersEntity).select().map {
            row -> UserDTO(
                row[id].toString(),
                row[username].toString(),
                row[email].toString(),
                row[nbCoins]?:0,
                null
            )
        }.toMutableList()
    }

    fun createUserTable(){
        val request="CREATE TABLE IF not exists utilisateur ( id uuid PRIMARY KEY, username VARCHAR(255), password VARCHAR(255),coins numeric,email VARCHAR(255), lastgift timestamp)"
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
                        row[nbCoins] ?: 0,
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
            set(it.lastGift,now())
        }
    }
    fun deleteUserByUsername(username: String): Boolean {
        val deletedCount = database.delete(UsersEntity) {
            it.username eq username
        }
        return deletedCount > 0
    }

    fun canHaveDailyGift(username: String): Boolean {
        val request = "SELECT CASE WHEN NOW() - lastgift > INTERVAL '1 day' THEN true ELSE false END AS is_lastgift_greater_than_1_day FROM utilisateur WHERE username = '$username';"
        val resultSet = database.ExecuteWithResult(request)

        resultSet?.use {
            if (resultSet.next()) {
                val isDailyGift = resultSet.getBoolean("is_lastgift_greater_than_1_day")
                if (isDailyGift) {
                    database.update(UsersEntity) {
                        set(lastGift, now())
                        where { it.username eq username }
                    }
                }
                return isDailyGift
            }
        }
        return false
    }

}


