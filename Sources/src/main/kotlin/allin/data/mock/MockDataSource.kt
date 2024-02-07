package allin.data.mock

import allin.data.AllInDataSource
import allin.data.BetDataSource
import allin.data.ParticipationDataSource
import allin.data.UserDataSource
import allin.model.Bet
import allin.model.BetResult
import allin.model.Participation
import allin.model.User
import java.time.ZonedDateTime

class MockDataSource : AllInDataSource() {

    class MockData {
        val bets by lazy { mutableListOf<Bet>() }
        val results by lazy { mutableListOf<BetResult>() }
        val resultNotifications by lazy { mutableListOf<Pair<String, String>>() }
        val users by lazy { mutableListOf<User>() }
        val lastGifts by lazy { mutableMapOf<String, ZonedDateTime>() }
        val participations by lazy { mutableListOf<Participation>() }
    }

    private val mockData by lazy { MockData() }

    override val userDataSource: UserDataSource = MockUserDataSource(mockData)
    override val betDataSource: BetDataSource = MockBetDataSource(mockData)
    override val participationDataSource: ParticipationDataSource = MockParticipationDataSource(mockData)
}
