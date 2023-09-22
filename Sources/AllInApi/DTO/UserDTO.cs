namespace AllInApi.DTO
{
    public class UserDTO
    {
        public string Name { get; set; }
        public string Mail { get; set; }
        public LargeImageDTO Image { get; set; }
        public int NumberOfCoins { get; set; }

        public List<GroupDTO> Groups { get; set;}
    }
}
