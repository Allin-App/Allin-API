package allin.utils

import allin.dto.UserDTO
import allin.model.User
import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import io.ktor.server.config.*
import java.util.*

class TokenManager private constructor(val config: HoconApplicationConfig) {

    val audience=config.property("audience").getString()
    val secret=config.property("secret").getString()
    val issuer=config.property("issuer").getString()
    fun generateJWTToken(user : User): String {
        val expirationDate = System.currentTimeMillis() + 604800000 // une semaine en miliseconde
        val token = JWT.create()
            .withAudience(audience)
            .withIssuer(issuer)
            .withClaim("username", user.username)
            .withExpiresAt(Date(expirationDate))
            .sign(Algorithm.HMAC256(secret))
        return token
    }

    fun verifyJWTToken(): JWTVerifier {
        return JWT.require(Algorithm.HMAC256(secret))
            .withAudience(audience)
            .withIssuer(issuer)
            .build()
    }

    fun generateOrReplaceJWTToken(user: User): String {
        val userToken = getUserToken(user)
        if (userToken != null && !isTokenExpired(userToken)) {
            return userToken
        } else {
            return generateJWTToken(user)
        }
    }

    fun generateOrReplaceJWTToken(user: UserDTO): String {
        val userToken = getUserToken(user)
        if (userToken != null && !isTokenExpired(userToken)) {
            return userToken
        } else {
            return generateJWTToken(user)
        }
    }

    fun generateJWTToken(user : UserDTO): String {
        val expirationDate = System.currentTimeMillis() + 604800000 // une semaine en miliseconde
        val token = JWT.create()
            .withAudience(audience)
            .withIssuer(issuer)
            .withClaim("username", user.username)
            .withExpiresAt(Date(expirationDate))
            .sign(Algorithm.HMAC256(secret))
        return token
    }

    fun isTokenExpired(token: String): Boolean {
        val expirationTime = JWT.decode(token).expiresAt.time
        return System.currentTimeMillis() > expirationTime
    }

    fun getUserToken(user: User): String? {
        return user.token
    }
    fun getUserToken(user: UserDTO): String? {
        return user.token
    }
    fun getUsernameFromToken(token: String) : String{
        val decodedJWT: DecodedJWT = JWT.decode(token)
        return decodedJWT.getClaim("username").asString()
    }
    companion object {
        private var instance: TokenManager? = null
        fun getInstance(config: HoconApplicationConfig): TokenManager {
            return instance ?: synchronized(this) {
                instance ?: TokenManager(config).also { instance = it }
            }
        }
    }
}