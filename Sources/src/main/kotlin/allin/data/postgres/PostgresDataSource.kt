package allin.data.postgres

import allin.data.AllInDataSource
import allin.data.BetDataSource
import allin.data.ParticipationDataSource
import allin.data.UserDataSource
import org.ktorm.database.Database

class PostgresDataSource : AllInDataSource() {

    private val database: Database

    init {
        val dbDatabase = System.getenv()["POSTGRES_DB"]
        val dbUser = System.getenv()["POSTGRES_USER"]
        val dbPassword = System.getenv()["POSTGRES_PASSWORD"]
        val dbHost = System.getenv()["POSTGRES_HOST"]

        database = Database.connect(
            url = "jdbc:postgresql://$dbHost/$dbDatabase",
            user = dbUser,
            password = dbPassword
        )
    }

    override val userDataSource: UserDataSource = PostgresUserDataSource(database)
        .also { it.createUserTable() }

    override val betDataSource: BetDataSource = PostgresBetDataSource(database)
        .also { it.createBetsTable() }

    override val participationDataSource: ParticipationDataSource = PostgresParticipationDataSource(database)
        .also { it.createParticipationTable() }
}