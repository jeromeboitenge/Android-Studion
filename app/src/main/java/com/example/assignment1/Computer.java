package com.example.assignment1;

public class Computer {
    private long id;
    private String model;
    private double price;
    private String purchaseDate;
    private boolean isLaptop;
    private String imageUri;
    private long brandId;

    // Helper to store Brand Name if needed for display, though standard way is to
    // query
    private String brandName;

    public Computer(long id, String model, double price, String purchaseDate, boolean isLaptop, String imageUri,
            long brandId) {
        this.id = id;
        this.model = model;
        this.price = price;
        this.purchaseDate = purchaseDate;
        this.isLaptop = isLaptop;
        this.imageUri = imageUri;
        this.brandId = brandId;
    }

    public Computer(String model, double price, String purchaseDate, boolean isLaptop, String imageUri, long brandId) {
        this.model = model;
        this.price = price;
        this.purchaseDate = purchaseDate;
        this.isLaptop = isLaptop;
        this.imageUri = imageUri;
        this.brandId = brandId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(String purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public boolean isLaptop() {
        return isLaptop;
    }

    public void setLaptop(boolean laptop) {
        isLaptop = laptop;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public long getBrandId() {
        return brandId;
    }

    public void setBrandId(long brandId) {
        this.brandId = brandId;
    }

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }
}
