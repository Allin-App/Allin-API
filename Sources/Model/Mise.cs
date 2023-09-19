using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Model
{
    public class Mise
    {
        public int Cost { get; set; }
        public string Choice { get; set; }

        public Mise(int cost, string choice) 
        { 
            Cost = cost;
            Choice = choice;
        }
    }
}
