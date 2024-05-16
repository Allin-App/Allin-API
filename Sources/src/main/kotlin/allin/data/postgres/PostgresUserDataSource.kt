package allin.data.postgres

import allin.data.UserDataSource
import allin.data.postgres.entities.UserEntity
import allin.data.postgres.entities.UsersEntity
import allin.data.postgres.entities.users
import allin.dto.UserDTO
import allin.ext.executeWithResult
import allin.model.User
import org.ktorm.database.Database
import org.ktorm.database.use
import org.ktorm.dsl.*
import org.ktorm.entity.add
import org.ktorm.entity.filter
import org.ktorm.entity.find
import org.ktorm.entity.removeIf
import java.time.Instant.now

class PostgresUserDataSource(private val database: Database) : UserDataSource {
    override fun getUserByUsername(username: String): Pair<UserDTO?, String?> =
        database.users
            .find { (it.username eq username) or (it.email eq username) }
            ?.let { it.toUserDTO() to it.password }
            ?: (null to null)

    override fun addUser(user: User) {
        database.users.add(
            UserEntity {
                this.id = user.id
                this.nbCoins = user.nbCoins
                this.username = user.username
                this.password = user.password
                this.email = user.email
                this.lastGift = now()
            }
        )
    }

    override fun deleteUser(username: String): Boolean =
        database.users.removeIf { (it.username eq username) or (it.email eq username) } > 0

    override fun addCoins(username: String, amount: Int) {
        database.users
            .find { it.username eq username }
            ?.set(UsersEntity.nbCoins.name, UsersEntity.nbCoins + amount)
    }

    override fun removeCoins(username: String, amount: Int) {
        database.users
            .find { it.username eq username }
            ?.set(UsersEntity.nbCoins.name, UsersEntity.nbCoins - amount)
    }

    override fun userExists(username: String) =
        database.users.filter {
            (it.username eq username)
        }.totalRecords > 0

    override fun emailExists(email: String) =
        database.users.filter {
            (it.email eq email)
        }.totalRecords > 0

    override fun canHaveDailyGift(username: String): Boolean {
        val request =
            "SELECT CASE WHEN DATE(NOW()) > DATE(lastgift) THEN true ELSE false END AS is_lastgift_greater_than_1_day FROM utilisateur WHERE username = '$username';"
        val resultSet = database.executeWithResult(request)

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