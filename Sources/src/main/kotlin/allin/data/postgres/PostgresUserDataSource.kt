package allin.data.postgres

import allin.data.UserDataSource
import allin.dto.UserDTO
import allin.entities.UsersEntity
import allin.model.User
import allin.utils.Execute
import org.ktorm.database.Database
import org.ktorm.dsl.*
import java.util.*

class PostgresUserDataSource(private val database: Database) : UserDataSource {
    override fun getUserByUsername(username: String): Pair<UserDTO?, String?> =
        database.from(UsersEntity)
            .select()
            .where { UsersEntity.username eq username }
            .map { row ->
                Pair(
                    UserDTO(
                        row[UsersEntity.id].toString(),
                        row[UsersEntity.username].toString(),
                        row[UsersEntity.email].toString(),
                        row[UsersEntity.nbCoins] ?: 0,
                        null
                    ),
                    row[UsersEntity.password].toString()
                )
            }
            .firstOrNull() ?: Pair(null, null)

    override fun addUser(user: User) {
        database.insert(UsersEntity) {
            set(it.id, UUID.fromString(user.id))
            set(it.nbCoins, user.nbCoins)
            set(it.username, user.username)
            set(it.password, user.password)
            set(it.email, user.email)
        }
    }

    override fun deleteUser(username: String): Boolean {
        val deletedCount = database.delete(UsersEntity) {
            it.username eq username
        }
        return deletedCount > 0
    }

    override fun userExists(username: String, email: String): Boolean {
        return database.from(UsersEntity).select(UsersEntity.username, UsersEntity.email).where {
            (UsersEntity.username eq username) and (UsersEntity.email eq email)
        }.totalRecords > 0
    }

    override fun modifyUserCoins(username: String, amount: Int) {
        val request = "UPDATE utilisateur SET coins = coins - $amount WHERE username = '$username';"
        database.Execute(request)
    }

    fun createUserTable() {
        val request =
            "CREATE TABLE IF not exists utilisateur ( id uuid PRIMARY KEY, username VARCHAR(255), password VARCHAR(255),coins double precision,email VARCHAR(255))"
        database.Execute(request)
    }
}