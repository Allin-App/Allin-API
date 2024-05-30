package allin.data.postgres.entities

import allin.model.UserImage
import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.Table
import org.ktorm.schema.bytes
import org.ktorm.schema.varchar

interface UserImageEntity : Entity<UserImageEntity> {
    companion object : Entity.Factory<UserImageEntity>()

    var id: String
    var image: ByteArray

    fun toUserImage() =
        UserImage(
            id = id,
            image = image,
        )
}

object UsersImageEntity : Table<UserImageEntity>("userimage") {
    val id = varchar("user_id").primaryKey().bindTo { it.id }
    val image = bytes("image").bindTo { it.image }
}

val Database.usersimage get() = this.sequenceOf(UsersImageEntity)
