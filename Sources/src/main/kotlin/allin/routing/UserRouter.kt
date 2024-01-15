package allin.routing

import allin.entities.UsersEntity.addUserEntity
import allin.entities.UsersEntity.deleteUserByUsername
import allin.entities.UsersEntity.getUserByUsernameAndPassword
import allin.entities.UsersEntity.getUserToUserDTO
import allin.model.*
import allin.utils.AppConfig
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.ktorm.database.Database

val RegexCheckerUser= AppConfig.regexChecker
val CryptManagerUser= AppConfig.cryptManager
val tokenManagerUser=AppConfig.tokenManager
val database = Database.connect("jdbc:postgresql://localhost:5432/Allin", user = "postgres", password = "lulu")
fun Application.UserRouter() {
    routing {
        route("/users/register"){
            post {
                val TempUser = call.receive<User>()
                if (RegexCheckerUser.isEmailInvalid(TempUser.email)){
                    call.respond(HttpStatusCode.Forbidden,"Input a valid mail !")
                }
                val users=getUserToUserDTO()
                val user = users.find { it.username == TempUser.username || it.email == TempUser.email }
                if(user == null) {
                    CryptManagerUser.passwordCrypt(TempUser)
                    TempUser.token=tokenManagerUser.generateOrReplaceJWTToken(TempUser)
                    addUserEntity(TempUser)
                    call.respond(HttpStatusCode.Created,TempUser)
                }
                call.respond(HttpStatusCode.Conflict,"Mail or/and username already exist")
            }
        }

        route("/users/login") {
            post {
                val checkUser = call.receive<CheckUser>()
                val user =getUserByUsernameAndPassword(checkUser.login)
                if (CryptManagerUser.passwordDecrypt(user.second.toString(),checkUser.password)) {
                    val userDtoWithToken=user.first
                    userDtoWithToken?.token=tokenManagerUser.generateOrReplaceJWTToken(userDtoWithToken!!)
                    call.respond(HttpStatusCode.OK, userDtoWithToken)
                } else {
                    call.respond(HttpStatusCode.NotFound,"Login and/or password incorrect.")
                }
            }
        }
        route("/users/delete") {
            post {
                val checkUser = call.receive<CheckUser>()
                val user =getUserByUsernameAndPassword(checkUser.login)
                if (CryptManagerUser.passwordDecrypt(user.second.toString(),checkUser.password)) {
                    if (!deleteUserByUsername(checkUser.login)) {
                        call.respond(HttpStatusCode.InternalServerError, "This user can't be delete now !")
                    }
                    call.respond(HttpStatusCode.Accepted,user.first!!)
                } else {
                    call.respond(HttpStatusCode.NotFound, "Login and/or password incorrect.")
                }
            }
        }

        authenticate {
            get("/users/token") {
                val principal = call.principal<JWTPrincipal>()
                val username = principal!!.payload.getClaim("username").asString()
                val user=getUserByUsernameAndPassword(username)
                if (user.first != null) {
                    call.respond(HttpStatusCode.OK, user.first!!)
                } else {
                    call.respond(HttpStatusCode.NotFound, "User not found with the valid token !")
                }
            }
        }
    }
}
