package com.agency.cache;

import com.agency.db.ClientRepository;
import com.agency.db.TripRepository;
import com.agency.db.DocumentRepository;
import com.agency.model.Client;
import com.agency.model.Trip;
import com.agency.model.Document;

import java.util.ArrayList;
import java.util.List;

public class AppCache {

    private static List<Client> clients = new ArrayList<>();
    private static List<Trip> trips = new ArrayList<>();
    private static List<Document> documents = new ArrayList<>();

    private AppCache() {
        // prevent object creation
    }

    // Load once on app start
    public static void loadAll() {
        reloadClients();
        reloadTrips();
        reloadDocuments();
    }

    public static void reloadClients() {
        clients = ClientRepository.getAllClients();
    }

    public static void reloadTrips() {
        trips = TripRepository.getAllTrips();
    }

    public static void reloadDocuments() {
        documents = DocumentRepository.getAllDocuments();
    }

    public static List<Client> getClients() {
        return clients;
    }

    public static List<Trip> getTrips() {
        return trips;
    }

    public static List<Document> getDocuments() {
        return documents;
    }
}