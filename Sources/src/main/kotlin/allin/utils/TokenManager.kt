package allin.utils

import allin.model.User
import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.config.*
import java.util.*

class TokenManager (val config: HoconApplicationConfig){

    val audience=config.property("audience").getString()
    val secret=config.property("secret").getString()
    val issuer=config.property("issuer").getString()
    private fun generateJWTToken(user : User): String {
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

    private fun isTokenExpired(token: String): Boolean {
        val expirationTime = JWT.decode(token).expiresAt.time
        return System.currentTimeMillis() > expirationTime
    }

    private fun getUserToken(user: User): String? {
        return user.token
    }
}