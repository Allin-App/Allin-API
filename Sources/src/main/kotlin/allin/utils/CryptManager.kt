package allin.utils

import allin.model.User
import org.mindrot.jbcrypt.BCrypt

class CryptManager {
    val salt=BCrypt.gensalt()
    fun passwordCrypt(user: User){
        user.password=BCrypt.hashpw(user.password,salt)

    }
    fun passwordDecrypt(user: User, password: String): Boolean{
        return BCrypt.hashpw(password,salt)==user.password
    }
}