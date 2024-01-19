package allin.entities

import allin.database
import allin.dto.UserDTO
import allin.model.User
import allin.utils.Execute
import org.ktorm.dsl.*
import org.ktorm.entity.*
import org.ktorm.schema.Table
import org.ktorm.schema.double
import org.ktorm.schema.int
import org.ktorm.schema.varchar

interface UserEntity : Entity<UserEntity> {
    val username: String
    var email: String
    var password: String
    var nbCoins: Double
}
object UsersEntity : Table<UserEntity>("utilisateur") {
    val id = int("id").primaryKey()
    val username = varchar("username")
    val password = varchar("password")
    val nbCoins = double("coins")
    val email = varchar("email")

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
        val request="CREATE TABLE IF not exists utilisateur ( id uuid PRIMARY KEY, username VARCHAR(255), password VARCHAR(255),coins double precision,email VARCHAR(255))"
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
}


