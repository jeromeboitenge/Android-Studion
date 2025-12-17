package com.example.assignment1;

public class Computer {
    private long id;
    private String model; // Machine Name
    private String serialNumber;
    private String location;
    private String imageUri;
    private long brandId;
    private String brandName; // For display
    private String status; // Active/Inactive
    private String dateAdded; // New field

    public Computer() {
    }

    public Computer(long id, String model, String serialNumber, String location, String imageUri, long brandId,
            String dateAdded) {
        this.id = id;
        this.model = model;
        this.serialNumber = serialNumber;
        this.location = location;
        this.imageUri = imageUri;
        this.brandId = brandId;
        this.status = "Active";
        this.dateAdded = dateAdded;
    }

    public Computer(String model, String serialNumber, String location, String imageUri, long brandId,
            String dateAdded) {
        this.model = model;
        this.serialNumber = serialNumber;
        this.location = location;
        this.imageUri = imageUri;
        this.brandId = brandId;
        this.status = "Active";
        this.dateAdded = dateAdded;
    }

    // Constructor without date for backward compatibility (optional, but good
    // practice)
    public Computer(long id, String model, String serialNumber, String location, String imageUri, long brandId) {
        this(id, model, serialNumber, location, imageUri, brandId, null);
    }

    public Computer(String model, String serialNumber, String location, String imageUri, long brandId) {
        this(model, serialNumber, location, imageUri, brandId, null);
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

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(String dateAdded) {
        this.dateAdded = dateAdded;
    }
}
