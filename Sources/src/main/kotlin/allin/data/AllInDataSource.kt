package allin.data

abstract class AllInDataSource {
    abstract val userDataSource: UserDataSource
    abstract val betDataSource: BetDataSource
    abstract val participationDataSource: ParticipationDataSource
    abstract val friendDataSource: FriendDataSource
}