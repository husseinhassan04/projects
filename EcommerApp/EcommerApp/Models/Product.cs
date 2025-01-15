using System.ComponentModel.DataAnnotations;

namespace EcommerApp.Models
{
    public class Product
    {
        public int Id { get; set; }

        [Required]
        public string? Title { get; set; }

        public double Price { get; set; }

        public string? Desc { get; set; }

        public Product()
        {
                
        }

    }

}
