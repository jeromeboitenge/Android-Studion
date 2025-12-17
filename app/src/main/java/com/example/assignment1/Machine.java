package com.example.assignment1;

public class Machine {
    private long id;
    private String name;
    private String serialNumber;
    private String status;
    private String location;
    private String dateAdded;
    private long brandId;
    private String brandName;

    public Machine(long id, String name, String serialNumber, String status, String location, String dateAdded,
            long brandId, String brandName) {
        this.id = id;
        this.name = name;
        this.serialNumber = serialNumber;
        this.status = status;
        this.location = location;
        this.dateAdded = dateAdded;
        this.brandId = brandId;
        this.brandName = brandName;
    }

    // Fallback constructor
    public Machine(long id, String name, String serialNumber, String status, String location, String dateAdded) {
        this(id, name, serialNumber, status, location, dateAdded, -1, "Unknown");
    }

    // Legacy Constructor
    public Machine(long id, String name, String serialNumber, String status, String location) {
        this(id, name, serialNumber, status, location, null, -1, "Unknown");
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public String getStatus() {
        return status;
    }

    public String getLocation() {
        return location;
    }

    public String getDateAdded() {
        return dateAdded;
    }

    public long getBrandId() {
        return brandId;
    }

    public String getBrandName() {
        return brandName;
    }
}
