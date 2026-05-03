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

    // ================= GETTERS =================

    public int getId() {
        return id;
    }

    public int getTripId() {
        return tripId;
    }

    public String getFilePath() {
        return filePath;
    }

    // ================= SETTERS =================

    public void setId(int id) {
        this.id = id;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    // ================= UI HELPER =================

    @Override
    public String toString() {
        return filePath;
    }
}