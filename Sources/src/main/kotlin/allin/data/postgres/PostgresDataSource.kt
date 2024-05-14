package allin.data.postgres

import allin.data.*
import allin.ext.execute
import org.ktorm.database.Database

class PostgresDataSource : AllInDataSource() {

    private val database: Database

    init {
        val dbDatabase = System.getenv()["POSTGRES_DB"]
        val dbUser = System.getenv()["POSTGRES_USER"]
        val dbPassword = System.getenv()["POSTGRES_PASSWORD"]
        val dbHost = System.getenv()["POSTGRES_HOST"]
        val url = "jdbc:postgresql://$dbHost/$dbDatabase"

        println("APP STARTING ON POSTGRESQL DATA SOURCE $url")

        database = Database.connect(
            url = url,
            user = dbUser,
            password = dbPassword
        )

        database.execute(
            """
            CREATE TABLE IF not exists utilisateur ( 
                id VARCHAR(255) PRIMARY KEY, 
                username VARCHAR(255), 
                password VARCHAR(255),
                coins int,
                email VARCHAR(255),
                lastgift timestamp
            )
            """.trimIndent()
        )

        database.execute(
            """
            CREATE TABLE IF not exists bet (
                id VARCHAR(255) PRIMARY KEY, 
                theme VARCHAR(255), 
                endregistration timestamp,
                endbet timestamp,
                sentencebet varchar(500),
                isprivate boolean, 
                createdby varchar(250),
                status varchar(20),
                type varchar(20)
            )
            """.trimIndent()
        )

        database.execute(
            """
            CREATE TABLE IF NOT EXISTS betresult (
                betid VARCHAR(255) PRIMARY KEY REFERENCES bet,
                result varchar(250)
            )
            """.trimIndent()
        )

        database.execute(
            """
            CREATE TABLE IF NOT EXISTS betresultnotification (
                betid VARCHAR(255),
                username varchar(250),
                CONSTRAINT pk_id_username PRIMARY KEY (betid, username)
            )
            """.trimIndent()
        )

        database.execute(
            """
            CREATE TABLE IF NOT EXISTS participation (
                id VARCHAR(255) PRIMARY KEY,
                bet VARCHAR(255),
                username varchar(250),
                answer varchar(250),
                stake int
            )
            """.trimIndent()
        )

        database.execute(
            """
            CREATE TABLE IF NOT EXISTS response (
                id VARCHAR(255),
                response VARCHAR(250),
                CONSTRAINT pk_response_id PRIMARY KEY (id, response)
            )
            """.trimIndent()
        )

        database.execute(
            """
                CREATE TABLE IF NOT EXISTS friend(
                    sender VARCHAR(255),
                    receiver VARCHAR(255),
                    CONSTRAINT pk_friend PRIMARY KEY (sender,receiver)
               )
            """.trimIndent()
        )
    }

    override val userDataSource: UserDataSource by lazy { PostgresUserDataSource(database) }
    override val betDataSource: BetDataSource by lazy { PostgresBetDataSource(database) }
    override val participationDataSource: ParticipationDataSource by lazy { PostgresParticipationDataSource(database) }
    override val friendDataSource: FriendDataSource by lazy { PostgresFriendDataSource(database) }
}