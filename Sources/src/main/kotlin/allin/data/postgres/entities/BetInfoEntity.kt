package allin.data.postgres.entities

import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.varchar


interface BetInfoEntity : Entity<BetInfoEntity> {
    companion object : Entity.Factory<BetInfoEntity>()

    var id: String
    var totalStakes: Int
}

object BetInfosEntity : Table<BetInfoEntity>("betinfo") {
    val id = varchar("id").primaryKey().bindTo { it.id }
    val totalStakes = int("totalstakes").bindTo { it.totalStakes }
}

val Database.betInfos get() = this.sequenceOf(BetInfosEntity)
