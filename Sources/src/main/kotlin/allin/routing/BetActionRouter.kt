package allin.routing

import allin.model.BetAction
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.routing.*

fun Application.BetActionRouter(){
    routing {
        route("/BetAction/add"){
        }
    }
}
