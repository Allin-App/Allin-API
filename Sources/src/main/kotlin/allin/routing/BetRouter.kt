package allin.routing
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import allin.model.*
import io.ktor.http.*
import io.ktor.server.response.*

val bets = mutableListOf<Bet>()
fun Application.BetRouter(){
    routing{
        route("/bets/add"){
            post{
                val bet = call.receive<Bet>()
                val findbet = bets.find { it.id == bet.id }
                if(findbet==null){
                    bets.add(bet)
                    call.respond(HttpStatusCode.Created, bet)
                }
                call.respond(HttpStatusCode.Conflict,"Bet already exist")
            }
        }
        route("/bets/delete"){
            post{

            }
        }
    }
}