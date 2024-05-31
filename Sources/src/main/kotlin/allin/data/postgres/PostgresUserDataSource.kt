package allin.data.postgres

import allin.data.UserDataSource
import allin.data.postgres.entities.*
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
            ?.let { it.toUserDTO(database) to it.password }
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
        database.update(UsersEntity) {
            set(it.nbCoins, it.nbCoins + amount)
            where { it.username eq username }
        }
    }

    override fun removeCoins(username: String, amount: Int) {
        database.update(UsersEntity) {
            set(it.nbCoins, it.nbCoins - amount)
            where { it.username eq username }
        }
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
            "SELECT CASE WHEN DATE(NOW()) > DATE(lastgift) THEN true ELSE false END AS is_lastgift_greater_than_1_day FROM users WHERE username = '$username';"
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

    override fun addImage(userid: String, image: ByteArray) {
        database.usersimage.add(UserImageEntity {
            id = userid
            this.image = image
        })
    }

    override fun removeImage(userid: String) {
        database.usersimage.removeIf { it.id eq userid }
    }

    override fun getImage(userid: String): String? {
            val resultSet = database.executeWithResult("SELECT encode(image, 'base64') AS image FROM userimage WHERE user_id = '${userid}'")?: return null
            if (resultSet.next()) {
                val base64Image: String? = resultSet.getString("image")
                if (base64Image != null) {
                    return base64Image
                }
            }
        return null
    }
}