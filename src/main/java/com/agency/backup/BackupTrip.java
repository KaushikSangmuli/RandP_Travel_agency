package com.agency.backup;

import java.util.List;

public class BackupTrip {
    public int id; // UI/internal only
    public String uuid;
    public String clientUuid;

    public int clientId; // UI only
    public String clientName;
    public String destination;
    public String date;
    public String type;
    public String status;
    public double purchaseValue;
    public double sellValue;
    public String airlineName;
    public double serviceFee;

    public List<BackupDocument> documents;
}