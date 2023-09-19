using Shared;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Model
{
    public interface IDataManager
    {
        IUsersManager UsersMgr { get; }
        IBetsManager BetsMgr { get; }
        IRunesManager RunesMgr { get; }
    }

    public interface IUsersManager : IGenericDataManager<User?>
    {
        Task<int> GetItemByMail(string mail);
        Task<int> GetNbItemsByUser(Group? group);
        Task<IEnumerable<User?>> GetItemsByGroup(Group? group, int index, int count, string? orderingPropertyName = null, bool descending = false);
        Task<int> GetNbItemsByAllCoins(int allCoins);
        Task<IEnumerable<User?>> GetItemsByAllCoins(int allCoins, int index, int count, string? orderingPropertyName = null, bool descending = false);

    }

    public interface IBetsManager : IGenericDataManager<Bet?>
    {
        Task<int> GetNbItemsByUser(User? user);
        Task<IEnumerable<Bet?>> GetItemsByUser(User? user, int index, int count, string? orderingPropertyName = null, bool descending = false);
        Task<int> GetNbItemsByDescription(string description);
        Task<IEnumerable<Bet?>> GetItemsByDescription(string description, int index, int count, string? orderingPropertyName = null, bool descending = false);

    }

    public interface IRunesManager : IGenericDataManager<Rune?>
    {
        
    }
}
