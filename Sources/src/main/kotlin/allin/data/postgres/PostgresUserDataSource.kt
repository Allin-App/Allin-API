package allin.data.postgres

import allin.data.UserDataSource
import allin.data.postgres.entities.UsersEntity
import allin.dto.UserDTO
import allin.model.User
import allin.utils.ExecuteWithResult
import org.ktorm.database.Database
import org.ktorm.database.use
import org.ktorm.dsl.*
import java.time.Instant.now
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
            set(it.lastGift, now())
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

    override fun addCoins(username: String, amount: Int) {
        database.update(UsersEntity) {
            set(UsersEntity.nbCoins, UsersEntity.nbCoins + amount)
            where { UsersEntity.username eq username }
        }
    }

    override fun removeCoins(username: String, amount: Int) {
        database.update(UsersEntity) {
            set(UsersEntity.nbCoins, UsersEntity.nbCoins - amount)
            where { UsersEntity.username eq username }
        }
    }

    override fun canHaveDailyGift(username: String): Boolean {
        val request =
            "SELECT CASE WHEN DATE(NOW()) > DATE(lastgift) THEN true ELSE false END AS is_lastgift_greater_than_1_day FROM utilisateur WHERE username = '$username';"
        val resultSet = database.ExecuteWithResult(request)

        resultSet?.use {
            if (resultSet.next()) {
                val isDailyGift = resultSet.getBoolean("is_lastgift_greater_than_1_day")
                if (isDailyGift) {
                    database.update(UsersEntity) {
                        set(UsersEntity.lastGift, now())
                        where { it.username eq username }
                    }
                }
                return isDailyGift
            }
        }
        return false
    }

}