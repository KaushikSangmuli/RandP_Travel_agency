package com.agency.model;

import java.util.UUID;

public class Document {

    private int id;
    private String uuid;

    private String tripUuid;
    private String clientUuid;

    private String filePath;
    private String type;
    private String subType;

    public Document(int id, String tripUuid, String clientUuid,
                    String filePath, String type, String subType) {

        this.id = id;
        this.uuid = UUID.randomUUID().toString(); // default for new
        this.tripUuid = tripUuid;
        this.clientUuid = clientUuid;
        this.filePath = filePath;
        this.type = type;
        this.subType = subType;
    }

    // ===== GETTERS =====
    public int getId() { return id; }
    public String getUuid() { return uuid; }
    public String getTripUuid() { return tripUuid; }
    public String getClientUuid() { return clientUuid; }
    public String getFilePath() { return filePath; }
    public String getType() { return type; }
    public String getSubType() { return subType; }

    // ===== SETTERS =====
    public void setId(int id) { this.id = id; }
    public void setUuid(String uuid) { this.uuid = uuid; }
    public void setTripUuid(String tripUuid) { this.tripUuid = tripUuid; }
    public void setClientUuid(String clientUuid) { this.clientUuid = clientUuid; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public void setType(String type) { this.type = type; }
    public void setSubType(String subType) { this.subType = subType; }

    @Override
    public String toString() {
        return filePath;
    }
}