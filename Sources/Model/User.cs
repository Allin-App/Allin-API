using System.Collections.ObjectModel;
using System.Reflection.PortableExecutable;
using System.Security.Claims;
using System.Text;
using System.Xml.Linq;
using static System.Net.Mime.MediaTypeNames;

namespace Model
{
    public class User
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

        public string Pseudo
        {
            get => pseudo;
            set
            {
                if (value == null)
                {
                    pseudo = "";
                    return;
                }
                pseudo = value;
            }
        }
        private string pseudo = "";

        public string Mail
        {
            get => mail;
            set
            {
                if (value == null)
                {
                    mail = "";
                    return;
                }
                mail = value;
            }
        }
        private string mail = "";

        public string Password
        {
            get => password;
            set
            {
                if (value == null)
                {
                    password = "";
                    return;
                }
                password = value;
            }
        }
        private string password = "";

        public DateTime CreationDate { get; set; }

        public LargeImage Image { get; set; }
        public int AllCoins { get; set; }

        public User(string id, string pseudo, string mail, string password, DateTime date, string image = "", int allCoins = 0) 
        {
            Id = id;
            Pseudo = pseudo;
            Mail = mail;
            Password = password;
            Image = new LargeImage(image);
            AllCoins = allCoins;
            Groups = new ReadOnlyCollection<Group>(groups);
        }

        public ReadOnlyCollection<Group> Groups { get; private set; }
        private List<Group> groups = new();

        public bool AddGroup(Group group)
        {
            if (groups.Contains(group))
                return false;
            groups.Add(group);
            return true;
        }

        public bool RemoveSkin(Group group)
            => groups.Remove(group);

        public override bool Equals(object? obj)
        {
            if (ReferenceEquals(obj, null)) return false;
            if (ReferenceEquals(obj, this)) return true;
            if (GetType() != obj.GetType()) return false;
            return Equals(obj as User);
        }

        public override int GetHashCode()
            => Id.GetHashCode() % 997;

        public bool Equals(User? other)
            => Id.Equals(other?.Id);

        public override string ToString()
        {
            StringBuilder sb = new StringBuilder($"{Id} ({Mail})");
            if (!string.IsNullOrWhiteSpace(Image.Base64))
            {
                sb.AppendLine($"\t{Image.Base64}");
            }
            if (Groups.Any())
            {
                sb.AppendLine("\tGroup:");
                foreach (var group in Groups)
                {
                    sb.AppendLine($"\t\t{group.Id} - {group.Name}");
                }
            }
            return sb.ToString();
        }
    }
}