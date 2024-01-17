package allin.routing

import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.BetActionRouter(){
    routing {
        route("/BetAction/add"){
        }
    }
}
