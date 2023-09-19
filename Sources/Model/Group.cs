using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Model
{
    public class Group
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

        public string Name
        {
            get => name;
            set
            {
                if (value == null)
                {
                    name = "";
                    return;
                }
                name = value;
            }
        }
        private string name = "";

        public LargeImage Image { get; set; }

        public DateTime CreationDate { get; set; }

        public Group(string id, string name, DateTime creationDate, string image = "") 
        {
            Id = id;
            Name = name;
            Image = new LargeImage(image);
            CreationDate = creationDate;
        }

        public override bool Equals(object? obj)
        {
            if (ReferenceEquals(obj, null)) return false;
            if (ReferenceEquals(obj, this)) return true;
            if (GetType() != obj.GetType()) return false;
            return Equals(obj as Group);
        }

        public override int GetHashCode()
            => Id.GetHashCode() % 997;

        public bool Equals(Group? other)
            => Id.Equals(other?.Id);

    }
}
