import allin.data.postgres.PostgresUserDataSource
import allin.data.postgres.entities.UsersEntity
import allin.data.postgres.entities.users
import allin.ext.executeWithResult
import allin.model.User
import junit.framework.TestCase.*
import org.junit.jupiter.api.*
import org.ktorm.database.Database
import org.ktorm.dsl.eq
import org.ktorm.dsl.update
import org.ktorm.entity.find
import org.ktorm.support.postgresql.PostgreSqlDialect
import java.time.Instant
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class PostgresUserDataSourceTest {

    private lateinit var userDataSource: PostgresUserDataSource
    private lateinit var database: Database

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

        userDataSource = PostgresUserDataSource(database)

    }

    @AfterAll
    fun delUser() {
        userDataSource.deleteUser("JaneDoe")
    }

    @Test
    @Order(1)
    fun addUser() {
        val user = User(
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

        userDataSource.addUser(user)

        val addedUser = database.users.find { it.id eq "123" }
        assertNotNull(addedUser)
        assertEquals("JohnDoe", addedUser?.username)
    }

    @Test
    @Order(2)
    fun getUserByUsername() {
        val result = userDataSource.getUserByUsername("JohnDoe")

        assertNotNull(result.first)
        assertEquals("JohnDoe", result.first?.username)
        assertEquals("securePassword123", result.second)

        val resultS = userDataSource.getUserByUsername("nonexistent")

        assertNull(resultS.first)
        assertNull(resultS.second)
    }

    @Test
    @Order(3)
    fun getUserById() {
        val result = userDataSource.getUserById("123")

        assertNotNull(result)
        assertEquals("JohnDoe", result?.username)
    }

    @Test
    @Order(4)
    fun deleteUser() {
        val result = userDataSource.deleteUser("JohnDoe")

        assertTrue(result)

        val deletedUser = database.users.find { it.id eq "123" }
        assertNull(deletedUser)

        val resultS = userDataSource.deleteUser("nonexistent")
        assertFalse(resultS)
    }

    @Test
    @Order(5)
    fun addCoins() {
        userDataSource.addUser(
            User(
                id = "11111",
                username = "JaneDoe",
                email = "janedoe@example.com",
                password = "securePassword456",
                nbCoins = 1000,
                token = null,
                image = null,
                bestWin = 500,
                nbBets = 50,
                nbFriends = 10
            )
        )

        userDataSource.addCoins("JaneDoe", 500)

        val updatedUser = database.users.find { it.id eq "11111" }
        assertNotNull(updatedUser)
        assertEquals(1500, updatedUser?.nbCoins)
    }

    @Test
    @Order(6)
    fun removeCoins() {
        userDataSource.removeCoins("JaneDoe", 300)
        val updatedUser = database.users.find { it.id eq "11111" }
        assertNotNull(updatedUser)
        assertEquals(1200, updatedUser?.nbCoins)
    }

    @Test
    @Order(7)
    fun userExists() {
        val result = userDataSource.userExists("JaneDoe")
        assertTrue(result)
        val resultS = userDataSource.userExists("nonexistent")
        assertFalse(resultS)
    }

    @Test
    @Order(8)
    fun emailExists() {
        val result = userDataSource.emailExists("janedoe@example.com")

        assertTrue(result)

        val resultS = userDataSource.emailExists("nonexistent@example.com")

        assertFalse(resultS)
    }

    @Test
    @Order(9)
    fun canHaveDailyGift() {
        database.update(UsersEntity) {
            set(it.lastGift, Instant.now().minusSeconds(86400 * 2)) // 2 days ago
            where { it.username eq "JaneDoe" }
        }

        val result = userDataSource.canHaveDailyGift("JaneDoe")

        assertTrue(result)

        val resultS = userDataSource.canHaveDailyGift("JaneDoe")

        assertFalse(resultS)
    }

    @Test
    @Order(10)
    fun addImage() {
        val imageBytes = "sampleImage".toByteArray()

        userDataSource.addImage("11111", imageBytes)

        val resultSet = database.executeWithResult(
            """
            SELECT encode(image, 'base64') AS image 
            FROM userimage 
            WHERE user_id = '11111'
            """.trimIndent()
        )

        assertNotNull(resultSet)
        if (resultSet != null && resultSet.next()) {
            val image = resultSet.getString("image")
            assertEquals(Base64.getEncoder().encodeToString(imageBytes), image)
        }
    }

    @Test
    @Order(11)
    fun getImage() {
        val result = userDataSource.getImage("11111")

        assertNotNull(result)
    }

    @Test
    @Order(12)
    fun removeImage() {
        userDataSource.removeImage("11111")

        val resultSet = database.executeWithResult(
            """
            SELECT encode(image, 'base64') AS image 
            FROM userimage 
            WHERE user_id = '11111'
            """.trimIndent()
        )

        assertNotNull(resultSet)
        if (resultSet != null && resultSet.next()) {
            val image = resultSet.getString("image")
            assertNull(image)
        }
    }
}