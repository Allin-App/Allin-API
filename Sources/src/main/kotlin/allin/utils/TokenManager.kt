package allin.utils

import allin.dto.UserDTO
import allin.model.User
import allin.utils.TokenManager.Companion.Claims.USERNAME
import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.auth.jwt.*
import io.ktor.server.config.*
import java.util.*

class TokenManager private constructor(config: HoconApplicationConfig) {

    private val audience = config.property("audience").getString()
    private val secret = config.property("secret").getString()
    private val issuer = config.property("issuer").getString()
    private fun generateJWTToken(user: User): String {
        val expirationDate = System.currentTimeMillis() + 604800000 // une semaine en miliseconde

        return JWT.create()
            .withAudience(audience)
            .withIssuer(issuer)
            .withClaim(USERNAME, user.username)
            .withExpiresAt(Date(expirationDate))
            .sign(Algorithm.HMAC256(secret))
    }

    fun verifyJWTToken(): JWTVerifier {
        return JWT.require(Algorithm.HMAC256(secret))
            .withAudience(audience)
            .withIssuer(issuer)
            .build()
    }

    fun generateOrReplaceJWTToken(user: User): String {
        val userToken = getUserToken(user)
        return if (userToken != null && !isTokenExpired(userToken)) {
            userToken
        } else {
            generateJWTToken(user)
        }
    }


    fun generateOrReplaceJWTToken(user: UserDTO): String {
        val userToken = getUserToken(user)
        return if (userToken != null && !isTokenExpired(userToken)) {
            userToken
        } else {
            generateJWTToken(user)
        }
    }

    private fun generateJWTToken(user: UserDTO): String {
        val expirationDate = System.currentTimeMillis() + 604800000 // une semaine en miliseconde
        return JWT.create()
            .withAudience(audience)
            .withIssuer(issuer)
            .withClaim(USERNAME, user.username)
            .withExpiresAt(Date(expirationDate))
            .sign(Algorithm.HMAC256(secret))
    }


    private fun isTokenExpired(token: String): Boolean {
        val expirationTime = JWT.decode(token).expiresAt.time
        return System.currentTimeMillis() > expirationTime
    }

    private fun getUserToken(user: User): String? = user.token
    private fun getUserToken(user: UserDTO): String? = user.token

    fun getUsernameFromToken(principal: JWTPrincipal): String {
        return principal.payload.getClaim(USERNAME).asString()
    }

    companion object {
        object Claims {
            const val USERNAME = "username"
        }

        private var instance: TokenManager? = null
        fun getInstance(config: HoconApplicationConfig): TokenManager {
            return instance ?: synchronized(this) {
                instance ?: TokenManager(config).also { instance = it }
            }
        }
    }
}