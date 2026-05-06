package com.agency.model;

import java.util.UUID;

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
    private double serviceFee;
    private String uuid;
    private String clientUuid;

    public Trip(int clientId, String clientUuid, String clientName, String destination, String date,
                String type, String status, double purchaseValue, double sellValue,
                String airlineName, double serviceFee) {

        this.uuid = UUID.randomUUID().toString();
        this.clientId = clientId;
        this.clientUuid = clientUuid;
        this.clientName = clientName;
        this.destination = destination;
        this.date = date;
        this.type = type;
        this.status = status;
        this.purchaseValue = purchaseValue;
        this.sellValue = sellValue;
        this.airlineName = airlineName;
        this.serviceFee = serviceFee;
    }

    // ================= GETTERS =================

    public int getId() { return id; }
    public int getClientId() { return clientId; }
    public String getClientName() { return clientName; }
    public String getDestination() { return destination; }
    public String getDate() { return date; }
    public String getType() { return type; }
    public String getStatus() { return status; }
    public double getPurchaseValue() { return purchaseValue; }
    public double getSellValue() { return sellValue; }
    public String getAirlineName() { return airlineName; }
    public double getServiceFee() { return serviceFee; }
    public String getUuid() { return uuid; }
    public String getClientUuid() { return clientUuid; }

    // ================= SETTERS =================

    public void setId(int id) { this.id = id; }
    public void setDestination(String destination) { this.destination = destination; }
    public void setDate(String date) { this.date = date; }
    public void setType(String type) { this.type = type; }
    public void setStatus(String status) { this.status = status; }
    public void setPurchaseValue(double purchaseValue) { this.purchaseValue = purchaseValue; }
    public void setSellValue(double sellValue) { this.sellValue = sellValue; }
    public void setAirlineName(String airlineName) { this.airlineName = airlineName; }
    public void setUuid(String uuid) { this.uuid = uuid; }
    public void setClientUuid(String clientUuid) { this.clientUuid = clientUuid; }
    public void setServiceFee(double serviceFee) { this.serviceFee = serviceFee; }

    // ================= BUSINESS LOGIC =================

    public double getProfit() {

        if (status == null) return 0;

        if ("Cancelled".equalsIgnoreCase(status)) {
            return serviceFee; // profit from cancellation
        }

        return sellValue - purchaseValue; // normal trip profit
    }

    // ================= UI HELPER =================

    @Override
    public String toString() {
        return clientName + " - " + destination;
    }
}