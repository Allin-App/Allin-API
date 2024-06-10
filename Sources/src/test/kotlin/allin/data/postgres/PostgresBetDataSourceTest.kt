package allin.data.postgres

import allin.data.postgres.entities.betAnswerInfos
import allin.data.postgres.entities.betInfos
import allin.data.postgres.entities.betResults
import allin.model.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.entity.removeIf
import org.ktorm.support.postgresql.PostgreSqlDialect
import java.time.ZoneId
import java.time.ZonedDateTime

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class PostgresBetDataSourceTest {

    private lateinit var database: Database
    private lateinit var dataSource: PostgresBetDataSource
    lateinit var user: User

    @BeforeAll
    fun setUp() {
        val dbDatabase = System.getenv()["POSTGRES_DB"]
        val dbUser = System.getenv()["POSTGRES_USER"]
        val dbPassword = System.getenv()["POSTGRES_PASSWORD"]
        val dbHost = System.getenv()["POSTGRES_HOST"]
        val url = "jdbc:postgresql://$dbHost/$dbDatabase"

        database = Database.connect(
            url = url,
            user = dbUser,
            password = dbPassword,
            dialect = PostgreSqlDialect()
        )

        user = User(
            id = "123",
            username = "JohnDoe",
            email = "johndoe@example.com",
            password = "securePassword123",
            nbCoins = 1000,
            token = null,
            image = null,
            bestWin = 500,
            nbBets = 50,
            nbFriends = 10
        )


        dataSource = PostgresBetDataSource(database)
        PostgresUserDataSource(database).addUser(user)
    }

    @Test
    @Order(1)
    fun testAddBet() {
        val bet = Bet(
            id = "bbba08f7-744f-4d23-9706-b31bdf24f614",
            theme = "Sports",
            sentenceBet = "Will team A win?",
            type = BetType.BINARY,
            endRegistration = ZonedDateTime.now(ZoneId.of("+02:00")).plusDays(1),
            endBet = ZonedDateTime.now(ZoneId.of("+02:00")).plusDays(2),
            isPrivate = false,
            response = listOf(YES_VALUE, NO_VALUE),
            createdBy = user.id
        )
        dataSource.addBet(bet)

        val retrievedBet = dataSource.getBetById("bbba08f7-744f-4d23-9706-b31bdf24f614")
        assertNotNull(retrievedBet)
        assertEquals("bbba08f7-744f-4d23-9706-b31bdf24f614", retrievedBet?.id)
    }

    @Test
    @Order(2)
    fun testGetAllBets() {
        val userDTO = user.toDto()
        val bets = dataSource.getAllBets(emptyList(), userDTO)
        assertTrue(bets.isNotEmpty())
    }

    @Test
    @Order(3)
    fun testUpdateBet() {
        val updatedData = UpdatedBetData(
            id = "bbba08f7-744f-4d23-9706-b31bdf24f614",
            endBet = ZonedDateTime.now(ZoneId.of("+02:00")).plusDays(3),
            isPrivate = true,
            response = listOf(YES_VALUE, NO_VALUE)
        )
        val result = dataSource.updateBet(updatedData)
        assertTrue(result)

        val retrievedBet = dataSource.getBetById("bbba08f7-744f-4d23-9706-b31bdf24f614")
        assertNotNull(retrievedBet)
        assertTrue(retrievedBet?.isPrivate ?: false)
    }

    @Test
    @Order(4)
    fun testConfirmBet() {
        dataSource.confirmBet("bbba08f7-744f-4d23-9706-b31bdf24f614", YES_VALUE)

        val retrievedBet = dataSource.getBetById("bbba08f7-744f-4d23-9706-b31bdf24f614")
        assertNotNull(retrievedBet)
        assertEquals(BetStatus.FINISHED, retrievedBet?.status)
    }

    @Test
    @Order(5)
    fun testGetBetDetailById() {
        val betDetail = dataSource.getBetDetailById("bbba08f7-744f-4d23-9706-b31bdf24f614", user.id)
        assertNotNull(betDetail)
        assertEquals("bbba08f7-744f-4d23-9706-b31bdf24f614", betDetail?.bet?.id)
    }

    @AfterAll
    fun tearDown() {
        database.betResults.removeIf { it.betId eq "bbba08f7-744f-4d23-9706-b31bdf24f614" }
        database.betInfos.removeIf { it.id eq "bbba08f7-744f-4d23-9706-b31bdf24f614" }
        database.betAnswerInfos.removeIf { it.betId eq "bbba08f7-744f-4d23-9706-b31bdf24f614" }
        dataSource.removeBet("bbba08f7-744f-4d23-9706-b31bdf24f614")
        PostgresUserDataSource(database).deleteUser(user.username)
    }
}
