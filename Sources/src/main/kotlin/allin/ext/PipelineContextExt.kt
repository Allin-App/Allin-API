package allin.ext

import allin.data.UserDataSource
import allin.dto.UserDTO
import allin.model.ApiMessage
import allin.utils.TokenManager.Companion.Claims.USERNAME
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.util.pipeline.*

suspend fun PipelineContext<*, ApplicationCall>.hasToken(content: suspend (principal: JWTPrincipal) -> Unit) =
    call.principal<JWTPrincipal>()?.let { content(it) } ?: call.respond(HttpStatusCode.Unauthorized)

suspend fun PipelineContext<*, ApplicationCall>.verifyUserFromToken(
    userDataSource: UserDataSource,
    principal: JWTPrincipal,
    content: suspend (user: UserDTO, password: String) -> Unit
) {
    val username = principal.payload.getClaim(USERNAME).asString()
    val user = userDataSource.getUserByUsername(username)
    user.first?.let { content(it, user.second ?: "") }
        ?: call.respond(HttpStatusCode.NotFound, ApiMessage.TOKEN_USER_NOT_FOUND)
}