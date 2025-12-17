public class NetworkComputer {
    private long id;
    private String brandName;
    private String model;
    private double price;

    public NetworkComputer(long id, String brandName, String model, double price) {
        this.id = id;
        this.brandName = brandName;
        this.model = model;
        this.price = price;
    }

    public long getId() {
        return id;
    }

    public String getBrandName() {
        return brandName;
    }

    public String getModel() {
        return model;
    }

    public double getPrice() {
        return price;
    }
}
