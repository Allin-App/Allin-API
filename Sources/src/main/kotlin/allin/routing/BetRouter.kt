package allin.routing
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import allin.model.*
import allin.utils.AppConfig
import io.ktor.http.*
import io.ktor.server.response.*

val bets = mutableListOf<Bet>()
val tokenManagerBet= AppConfig.tokenManager

fun CreateId() : Int{
    return bets.size
}

fun Application.BetRouter(){
    routing{
        route("/bets/add"){
            post{
                val bet = call.receive<BetWithoutId>()
                val id = CreateId()
                val username = tokenManagerBet.getUsernameFromToken(bet.createdBy)
                val findbet = bets.find { it.id == id }
                if(findbet==null){
                    val betWithId = convertBetWithoutIdToBet(bet,id,username)
                    bets.add(betWithId)
                    call.respond(HttpStatusCode.Created, betWithId)
                }
                call.respond(HttpStatusCode.Conflict,"Bet already exist")
            }
        }
        route("/bets/gets"){
            get{
                call.respond(HttpStatusCode.Accepted, bets)
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