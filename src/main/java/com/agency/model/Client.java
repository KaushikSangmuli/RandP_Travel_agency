package com.agency.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Client {

    private int id; // ❗ removed final (important)

    private final StringProperty name;
    private final StringProperty phone;
    private final StringProperty email;
    private final StringProperty city;

    public Client(int id, String name, String phone, String email, String city) {
        this.id = id;
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