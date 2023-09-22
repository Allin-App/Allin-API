namespace AllInApi.DTO
{
    public class BetDTO
    {
        public string Name { get; set; }
        public string Title { get; set; }
        public List<string> Choices { get; set; }
        public string Themes{ get; set; }
        public Dictionary<UserDTO, MiseDTO>? Bets { get; set; }

    }
}
