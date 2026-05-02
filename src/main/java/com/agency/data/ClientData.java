package com.agency.data;

import com.agency.model.Client;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ClientData {
    public static ObservableList<Client> clients = FXCollections.observableArrayList();

    private static int nextId = 1;

    public static int generateId() {
        return nextId++;
    }

}