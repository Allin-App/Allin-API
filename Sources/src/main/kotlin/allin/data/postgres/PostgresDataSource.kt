package allin.data.postgres

import allin.data.*
import allin.ext.execute
import org.ktorm.database.Database
import org.ktorm.support.postgresql.PostgreSqlDialect

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
            password = dbPassword,
            dialect = PostgreSqlDialect()
        )

        database.execute(
            """
                CREATE EXTENSION IF not exists fuzzystrmatch;
            """.trimIndent()
        )

        database.execute(
            """
            CREATE TABLE IF not exists users ( 
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
                zoneid varchar(500),
                sentencebet varchar(500),
                isprivate boolean, 
                createdby varchar(250),
                status varchar(20),
                type varchar(20),
                popularityscore numeric
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
                userid varchar(250),
                CONSTRAINT pk_id_username PRIMARY KEY (betid, userid)
            )
            """.trimIndent()
        )

        database.execute(
            """
            CREATE TABLE IF NOT EXISTS participation (
                id VARCHAR(255) PRIMARY KEY,
                bet VARCHAR(255),
                userid varchar(250),
                answer varchar(250),
                stake int
            )
            """.trimIndent()
        )

        database.execute(
            """
            CREATE TABLE IF NOT EXISTS response (
                betId VARCHAR(255),
                response VARCHAR(250),
                CONSTRAINT pk_response_id PRIMARY KEY (betId, response)
            )
            """.trimIndent()
        )

        database.execute(
            """
            CREATE TABLE IF not exists betInfo ( 
                id VARCHAR(255) PRIMARY KEY, 
                totalStakes int,
                totalParticipants int
            )
            """.trimIndent()
        )

        database.execute(
            """
            CREATE TABLE IF not exists betAnswerInfo ( 
                betId VARCHAR(255), 
                response VARCHAR(255), 
                totalStakes int,
                odds float,
                CONSTRAINT pk_bet_answer_info_id PRIMARY KEY (betId, response)
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

        database.execute(
            """
                CREATE TABLE IF NOT EXISTS userimage
                (
                    user_id VARCHAR(255) PRIMARY KEY,
                    image bytea
                )
            """.trimIndent()
        )

        database.execute(
            """
                CREATE TABLE IF NOT EXISTS privatebet
                (
                    betid VARCHAR(255),
                    userid VARCHAR(255),
                    CONSTRAINT pk_privatebet PRIMARY KEY (betid,userid)
                )
            """.trimIndent()
        )
    }

    override val userDataSource: UserDataSource by lazy { PostgresUserDataSource(database) }
    override val betDataSource: BetDataSource by lazy { PostgresBetDataSource(database) }
    override val participationDataSource: ParticipationDataSource by lazy { PostgresParticipationDataSource(database) }
    override val friendDataSource: FriendDataSource by lazy { PostgresFriendDataSource(database) }
}