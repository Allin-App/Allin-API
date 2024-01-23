package allin.utils

import allin.model.User
import org.mindrot.jbcrypt.BCrypt

class CryptManager {
    val salt=addDollarsSecrets(System.getenv().get("SALT").toString())
    // Cette fonction permet de remettre les $ que drone supprime dans les secrets drone
    fun addDollarsSecrets(chaine: String): String {
        val stringBuilder = StringBuilder(chaine)
        stringBuilder.insert(0, '$')
        stringBuilder.insert(3, '$')
        stringBuilder.insert(6, '$')

        return stringBuilder.toString()
    }
    fun passwordCrypt(password : String): String {
        return BCrypt.hashpw(password,salt)
    }
    fun passwordCrypt(user: User){
        user.password=BCrypt.hashpw(user.password,salt)
    }
    fun passwordDecrypt(password: String, passwordClear: String): Boolean{
        return BCrypt.hashpw(passwordClear,salt)==password
    }

    fun CheckPassword(hashed: String, clear: String): Boolean{
        return BCrypt.checkpw(hashed,clear)
    }
}