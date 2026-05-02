package com.agency.model;

public class Trip {
    private int id;
    private int clientId;
    private String clientName;
    private String destination;
    private String date;
    private String type;
    private String status;
    private double purchaseValue;
    private double sellValue;
    private String airlineName;
    private String documentPath;

    public Trip(){

    }


    public Trip( int clientId, String clientName, String destination, String date, String type,
                String status, double purchaseValue, double sellValue, String airlineName) {
        this.clientId = clientId;
        this.clientName = clientName;
        this.destination = destination;
        this.date = date;
        this.type = type;
        this.status = status;
        this.purchaseValue = purchaseValue;
        this.sellValue = sellValue;
        this.airlineName = airlineName;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }


    public int getClientId() { return clientId; }
    public String getClientName() { return clientName; }
    public String getDestination() { return destination; }
    public String getDate() { return date; }
    public String getType() { return type; }
    public String getStatus() { return status; }
    public double getPurchaseValue() { return purchaseValue; }
    public double getSellValue() { return sellValue; }
    public String getAirlineName() { return airlineName; }

    public double getProfit() {
        return sellValue - purchaseValue;
    }

    public String getDocumentPath() {
        return documentPath;
    }

    public void setDocumentPath(String documentPath) {
        this.documentPath = documentPath;
    }
}