package allin.routing

import allin.model.CheckUser
import allin.model.User
import allin.model.isEmailValid
import io.ktor.client.utils.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

val users = mutableListOf<User>()

fun Application.UserRouter() {

    routing {
        route("/users/register"){
            post {
                val TempUser = call.receive<User>()
                if (isEmailValid(TempUser.email)){
                    call.respond(HttpStatusCode.Forbidden,"Input a valid mail !")
                }
                val user = users.find { it.username == TempUser.username || it.email == TempUser.email }
                if(user == null) {
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
                if (user != null && user.password == checkUser.password) {
                    call.respond(HttpStatusCode.OK, user)
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
                    call.respond(HttpStatusCode.Accepted, user)
                } else {
                    call.respond(HttpStatusCode.NotFound,"Login and/or password incorrect.")
                }
            }
        }
    }
}
// REGISTER 201 created 400 bad request
// LOGIN 200 OK 404
