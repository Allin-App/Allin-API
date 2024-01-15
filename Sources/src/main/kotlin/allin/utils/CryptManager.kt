package allin.utils

import allin.model.User
import org.mindrot.jbcrypt.BCrypt

class CryptManager {
    //val salt=BCrypt.gensalt()
    fun passwordCrypt(password : String): String {
        return BCrypt.hashpw(password,"\$2a\$10\$42wsdBeoLKaF6SM9oADONe")
    }
    fun passwordCrypt(user: User){
        user.password=BCrypt.hashpw(user.password,"\$2a\$10\$42wsdBeoLKaF6SM9oADONe")
    }
    fun passwordDecrypt(password: String, passwordClear: String): Boolean{
        return BCrypt.hashpw(passwordClear,"\$2a\$10\$42wsdBeoLKaF6SM9oADONe")==password
    }

    fun CheckPassword(hashed: String, clear: String): Boolean{
        return BCrypt.checkpw(hashed,clear)
    }
}