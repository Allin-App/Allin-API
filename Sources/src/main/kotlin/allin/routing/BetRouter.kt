package allin.routing
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import allin.model.*
import allin.utils.AppConfig
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import java.time.ZonedDateTime

val bets = mutableListOf<Bet>()
val tokenManagerBet= AppConfig.tokenManager

fun CreateId() : Int{
    return bets.size
}

fun Application.BetRouter(){
    routing{
        authenticate {
        route("/bets/add"){
            post{
                val bet = call.receive<Bet>()
                val token= call.principal<JWTPrincipal>()
                val id = CreateId()
                bet.createdBy = tokenManagerBet.getUsernameFromToken(token.toString())
                val findbet = bets.find { it.id == id }
                if(findbet==null){
                    bets.add(bet)
                    call.respond(HttpStatusCode.Created, bet)
                }
                call.respond(HttpStatusCode.Conflict,"Bet already exist")
            }
        }
        }
        route("/bets/gets"){
            get{
               // if(bets.size>0)
                    call.respond(HttpStatusCode.Accepted, bets.toList())
               // else call.respond(HttpStatusCode.NoContent)
            }
        }
        route("/bets/delete"){
            post{
                val idbet = call.receive<Map<String, Int>>()["id"]
                val findbet = bets.find { it.id == idbet }
                if(findbet==null){
                    call.respond(HttpStatusCode.NotFound, "Bet doesnt find")
                }
                bets.remove(findbet)
                findbet as Bet
                call.respond(HttpStatusCode.Accepted, findbet)
            }
        }
        route("bets/update"){
            post{
                val updatedBetData = call.receive<UpdatedBetData>()
                val findbet = bets.find { it.id == updatedBetData.id }
                if (findbet == null) {
                    call.respond(HttpStatusCode.NotFound, "Bet not found")
                } else {
                    findbet.endBet = updatedBetData.endBet
                    findbet.isPrivate = updatedBetData.isPrivate
                    findbet.response = updatedBetData.response
                    call.respond(HttpStatusCode.Accepted, findbet)
                }
            }
        }
    }
}