package allin.data.mock

import allin.data.AllInDataSource
import allin.data.BetDataSource
import allin.data.ParticipationDataSource
import allin.data.UserDataSource

class MockDataSource : AllInDataSource() {
    override val userDataSource: UserDataSource = MockUserDataSource()
    override val betDataSource: BetDataSource = MockBetDataSource()
    override val participationDataSource: ParticipationDataSource = MockParticipationDataSource()
}
