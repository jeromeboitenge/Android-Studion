package com.example.assignment1;

public class Train {
    private String name;
    private String number;
    private String sourceCode;
    private String destCode;
    private String departureTime;
    private String arrivalTime;
    private String duration;

    public Train(String name, String number, String sourceCode, String destCode, String departureTime, String arrivalTime, String duration) {
        this.name = name;
        this.number = number;
        this.sourceCode = sourceCode;
        this.destCode = destCode;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.duration = duration;
    }

    // Getters
    public String getName() { return name; }

    public String getNumber() { return number; }

    // This is the method that was missing causing your error
    public String getSourceCode() { return sourceCode; }

    public String getDestCode() { return destCode; }

    public String getDepartureTime() { return departureTime; }

    public String getArrivalTime() { return arrivalTime; }

    public String getDuration() { return duration; }
}