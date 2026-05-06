package com.agency.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;import java.util.UUID;


public class Client {

    private int id; //  removed final (important)

    private final StringProperty name;
    private final StringProperty phone;
    private final StringProperty email;
    private final StringProperty city;
    private String uuid;

    public Client(int id, String name, String phone, String email, String city) {
        this.id = id;
        this.uuid = UUID.randomUUID().toString();
        this.name = new SimpleStringProperty(name);
        this.phone = new SimpleStringProperty(phone);
        this.email = new SimpleStringProperty(email);
        this.city = new SimpleStringProperty(city);
    }


    public int getId() {
        return id;
    }

    public String getName() {
        return name.get();
    }

    public String getPhone() {
        return phone.get();
    }

    public String getEmail() {
        return email.get();
    }

    public String getCity() {
        return city.get();
    }
    public String getUuid() { return uuid; }

    public void setUuid(String uuid) { this.uuid = uuid;  }

    public void setId(int id) {
        this.id = id;
    }
    public StringProperty nameProperty() {
        return name;
    }

    public StringProperty phoneProperty() {
        return phone;
    }

    public StringProperty emailProperty() {
        return email;
    }

    public StringProperty cityProperty() {
        return city;
    }
    @Override
    public String toString() {
        return getName() + " (" + getPhone() + ")";
    }
}