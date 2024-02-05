package allin.entities

import allin.database
import allin.utils.ExecuteWithResult
import org.ktorm.database.use
import org.ktorm.dsl.eq
import org.ktorm.dsl.update
import org.ktorm.entity.Entity
import org.ktorm.schema.*
import java.time.Instant

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
}


/*CREATE TABLE IF not exists utilisateur ( id uuid PRIMARY KEY, username VARCHAR(255), password VARCHAR(255),coins numeric,email VARCHAR(255), lastgift timestamp)*/
/*
fun canHaveDailyGift(username: String): Boolean {
    val request = "SELECT CASE WHEN NOW() - lastgift > INTERVAL '1 day' THEN true ELSE false END AS is_lastgift_greater_than_1_day FROM utilisateur WHERE username = '$username';"
    val resultSet = database.ExecuteWithResult(request)

    resultSet?.use {
        if (resultSet.next()) {
            val isDailyGift = resultSet.getBoolean("is_lastgift_greater_than_1_day")
            if (isDailyGift) {
                database.update(UsersEntity) {
                    set(UsersEntity.lastGift, Instant.now())
                    where { it.username eq username }
                }
            }
            return isDailyGift
        }
    }
    return false
}
*/

