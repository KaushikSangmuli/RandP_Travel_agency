package com.agency.model;

public class Document {

    private int id;
    private int tripId;
    private String filePath;

    public Document(int id, int tripId, String filePath) {
        this.id = id;
        this.tripId = tripId;
        this.filePath = filePath;
    }

    public int getId() {
        return id;
    }

    public int getTripId() {
        return tripId;
    }

    public String getFilePath() {
        return filePath;
    }
}