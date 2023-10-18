package allin.routing

import allin.dto.*
import allin.model.CheckUser
import allin.model.User
import allin.utils.CryptManager
import com.typesafe.config.ConfigFactory
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.config.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import allin.utils.RegexChecker
import allin.utils.TokenManager

val users = mutableListOf<User>()
val tokenManager= TokenManager(HoconApplicationConfig(ConfigFactory.load()))
val RegexChecker= RegexChecker()
val CryptManager= CryptManager()
fun Application.UserRouter() {

    routing {
        route("/users/register"){
            post {
                val TempUser = call.receive<User>()
                if (RegexChecker.isEmailInvalid(TempUser.email)){
                    call.respond(HttpStatusCode.Forbidden,"Input a valid mail !")
                }
                val user = users.find { it.username == TempUser.username || it.email == TempUser.email }
                if(user == null) {
                    CryptManager.passwordCrypt(TempUser)
                    users.add(TempUser)
                    call.respond(HttpStatusCode.Created, TempUser)
                }
                call.respond(HttpStatusCode.Conflict,"Mail or/and username already exist")
            }
        }

        route("/users/login") {
            post {
                val checkUser = call.receive<CheckUser>()
                val user = users.find { it.username == checkUser.login || it.email == checkUser.login }
                if (user != null && CryptManager.passwordDecrypt(user,checkUser.password)) {
                    user.token=tokenManager.generateOrReplaceJWTToken(user)
                    call.respond(HttpStatusCode.OK, convertUserToUserDTOToken(user))
                } else {
                    call.respond(HttpStatusCode.NotFound,"Login and/or password incorrect.")
                }
            }
        }

        route("/users/delete") {
            post {
                val checkUser = call.receive<CheckUser>()
                val user = users.find { it.username == checkUser.login || it.email == checkUser.login }
                if (user != null && user.password == checkUser.password) {
                    users.remove(user)
                    call.respond(HttpStatusCode.Accepted,convertUserToUserDTO(user))
                } else {
                    call.respond(HttpStatusCode.NotFound,"Login and/or password incorrect.")
                }
            }
        }

        authenticate {
            get("/users/token") {
                val principal = call.principal<JWTPrincipal>()
                val username = principal!!.payload.getClaim("username").asString()
                val user = users.find { it.username == username }
                if (user != null) {
                    call.respond(HttpStatusCode.OK,convertUserToUserDTO(user))
                } else {
                    call.respond(HttpStatusCode.NotFound, "User not found with the valid token !")
                }
            }
        }

    }
}
