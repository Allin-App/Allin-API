package allin.dto
import kotlinx.serialization.Serializable
@Serializable
data class UserDTO(val username: String, val email: String, val nbCoins: Double, var token:String?)
