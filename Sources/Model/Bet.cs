using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Data;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Model
{
    public class Bet
    {
        public string Id
        {
            get => id;
            private init
            {
                if (string.IsNullOrWhiteSpace(value))
                {
                    id = "Unknown";
                    return;
                }
                id = value;
            }
        }
        private readonly string id = null!;

        public string Title
        {
            get => title;
            private init
            {
                if (string.IsNullOrWhiteSpace(value))
                {
                    title = "Unknown";
                    return;
                }
                title = value;
            }
        }
        private readonly string title = null!;

        public string Name
        {
            get => name;
            private init
            {
                if (string.IsNullOrWhiteSpace(value))
                {
                    name = "Unknown";
                    return;
                }
                name = value;
            }
        }
        private readonly string name = null!;

        public ReadOnlyDictionary<User, Mise> Users { get; private set; }
        private Dictionary<User, Mise> users = new Dictionary<User, Mise>();

        public ReadOnlyCollection<String> Choices { get; private set; }
        private List<String> choices = new();

        public string Theme
        {
            get => theme;
            private init
            {
                if (string.IsNullOrWhiteSpace(value))
                {
                    theme = "Unknown";
                    return;
                }
                theme = value;
            }
        }
        private readonly string theme = null!;

        public bool Status { get; set; }

        public string Description { get; set; }

        public DateTime StartDate { get; set; }

        public DateTime EndDate { get; set; }

        public Bet(string id, string title, string name, string theme, bool status, string description, DateTime startDate, DateTime endDate)
        {
            Id = id;
            Title = title;
            Name = name;
            Choices = new ReadOnlyCollection<String>(choices);
            Theme = theme;
            Status = status;
            Description = description;
            StartDate = startDate;
            EndDate = endDate;
            Users = new ReadOnlyDictionary<User, Mise>(users);
        }

    }
}
