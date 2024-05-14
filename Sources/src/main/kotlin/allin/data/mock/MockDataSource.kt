package allin.data.mock

import allin.data.*
import allin.model.Bet
import allin.model.BetResult
import allin.model.Participation
import allin.model.User
import java.time.ZonedDateTime

class MockDataSource : AllInDataSource() {

    init {
        println("APP STARTING ON MOCK DATA SOURCE")
    }

    class MockData {
        val bets by lazy { mutableListOf<Bet>() }
        val results by lazy { mutableListOf<BetResult>() }
        val resultNotifications by lazy { mutableListOf<Pair<String, String>>() }
        val users by lazy { mutableListOf<User>() }
        val lastGifts by lazy { mutableMapOf<String, ZonedDateTime>() }
        val participations by lazy { mutableListOf<Participation>() }
    }

    private val mockData by lazy { MockData() }

    override val userDataSource: UserDataSource by lazy { MockUserDataSource(mockData) }
    override val betDataSource: BetDataSource by lazy { MockBetDataSource(mockData) }
    override val participationDataSource: ParticipationDataSource by lazy { MockParticipationDataSource(mockData) }
    override val friendDataSource: FriendDataSource by lazy { MockFriendDataSource(mockData) }
}
