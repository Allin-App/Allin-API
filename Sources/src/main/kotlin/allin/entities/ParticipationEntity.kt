package allin.entities

import allin.database
import allin.model.Participation
import allin.utils.Execute
import org.ktorm.dsl.*
import org.ktorm.entity.Entity
import org.ktorm.schema.*
import java.util.*

interface ParticipationEntity : Entity<ParticipationEntity> {
    val id: String
    val betId: String
    val username: String
    val answer: String
    val stake: Int
}


object ParticipationsEntity : Table<BetEntity>("participation") {
    val id = uuid("id").primaryKey()
    val betId = uuid("bet")
    val username = varchar("username")
    val answer = varchar("answer")
    val stake = int("stake")

    fun createParticipationTable(){
        val request="CREATE TABLE IF NOT EXISTS participation (id uuid PRIMARY KEY,bet uuid,username varchar(250),answer varchar(250),stake int);"
        database.Execute(request)
    }

    fun addParticipationEntity(participation : Participation){
        database.insert(ParticipationsEntity){
            set(it.id, UUID.fromString(participation.id))
            set(it.betId,UUID.fromString(participation.betId))
            set(it.username,participation.username)
            set(it.answer,participation.answer)
            set(it.stake,participation.stake)
        }
    }

    fun getParticipationEntity(): MutableList<Participation> {
        return database.from(ParticipationsEntity).select().map {
            row -> Participation(
                row[id].toString(),
                row[betId].toString(),
                row[username].toString(),
                row[answer].toString(),
                row[stake]?:0,
            )
        }.toMutableList()
    }

    fun deleteParticipation(participation: Participation): Boolean {
        val deletedCount = database.delete(ParticipationsEntity) {
            it.id eq UUID.fromString(participation.id)
        }
        return deletedCount > 0
    }
}