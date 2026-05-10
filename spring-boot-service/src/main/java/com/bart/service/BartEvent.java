package com.bart.service;

public class BartEvent {

    private String station;
    private String line;
    private String destination;
    private int minutes;
    private String status;
    private String timestamp;

    public String getStation()     { return station; }
    public String getLine()        { return line; }
    public String getDestination() { return destination; }
    public int getMinutes()        { return minutes; }
    public String getStatus()      { return status; }
    public String getTimestamp()   { return timestamp; }

    public void setStation(String station)         { this.station = station; }
    public void setLine(String line)               { this.line = line; }
    public void setDestination(String destination) { this.destination = destination; }
    public void setMinutes(int minutes)            { this.minutes = minutes; }
    public void setStatus(String status)           { this.status = status; }
    public void setTimestamp(String timestamp)     { this.timestamp = timestamp; }
}