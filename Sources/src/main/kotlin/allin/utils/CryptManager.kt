package allin.utils

import allin.model.User
import org.mindrot.jbcrypt.BCrypt

class CryptManager {
    val salt=System.getenv().get("SALT")
    fun passwordCrypt(password : String): String {
        return BCrypt.hashpw(password,salt)
    }
    fun passwordCrypt(user: User){
        println(salt)
        user.password=BCrypt.hashpw(user.password,salt)
    }
    fun passwordDecrypt(password: String, passwordClear: String): Boolean{
        return BCrypt.hashpw(passwordClear,salt)==password
    }

    fun CheckPassword(hashed: String, clear: String): Boolean{
        return BCrypt.checkpw(hashed,clear)
    }
}