package allin.data.mock

import allin.data.*
import allin.model.*
import java.time.ZonedDateTime

class MockDataSource : AllInDataSource() {

    init {
        println("APP STARTING ON MOCK DATA SOURCE")
    }

    class MockData {
        val bets by lazy { mutableListOf<Bet>() }
        val betInfos by lazy { mutableListOf<BetInfo>() }
        val answerInfos by lazy { mutableListOf<BetAnswerInfo>() }
        val results by lazy { mutableListOf<BetResult>() }
        val resultNotifications by lazy { mutableListOf<Pair<String, String>>() }
        val users by lazy { mutableListOf<User>() }
        val lastGifts by lazy { mutableMapOf<String, ZonedDateTime>() }
        val participations by lazy { mutableListOf<Participation>() }
        val friends by lazy { mutableListOf<Friend>() }
    }

    private val mockData by lazy { MockData() }

    override val userDataSource: UserDataSource by lazy { MockUserDataSource(mockData) }
    override val betDataSource: BetDataSource by lazy { MockBetDataSource(mockData) }
    override val participationDataSource: ParticipationDataSource by lazy { MockParticipationDataSource(mockData) }
    override val friendDataSource: FriendDataSource by lazy { MockFriendDataSource(mockData) }
}
