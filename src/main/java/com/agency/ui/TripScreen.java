package com.agency.ui;

import com.agency.model.Client;
import com.agency.model.Trip;
import com.agency.db.ClientRepository;
import com.agency.db.TripRepository;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class TripScreen {

    private static final int PAGE_SIZE = 8;
    private static int currentPage = 0;

    public static VBox getView() {
        VBox root = new VBox(20);
        root.getStyleClass().add("client-root");
        showTripList(root);
        return root;
    }

    public static VBox getAddTripViewDirect() {
        VBox root = new VBox(22);
        root.getStyleClass().add("client-root");
        showTripForm(root, null);
        return root;
    }

    private static void showTripList(VBox root) {
        root.getChildren().clear();

        Label title = new Label("Trips");
        title.getStyleClass().add("client-title");

        DatePicker dateFilter = new DatePicker();
        dateFilter.setPromptText("Filter by date");

        Button clearFilter = new Button("Clear");
        Button addBtn = new Button("+ Add New Trip");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox header = new HBox(14, title, spacer, dateFilter, clearFilter, addBtn);
        header.setAlignment(Pos.CENTER_LEFT);

        TextField search = new TextField();
        search.setPromptText("Search trips...");

        TableView<Trip> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Columns
        TableColumn<Trip, Number> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleIntegerProperty(data.getValue().getId())
        );

        TableColumn<Trip, String> clientCol = col("Client", "clientName");
        TableColumn<Trip, String> destCol = col("Destination", "destination");
        TableColumn<Trip, String> dateCol = col("Date", "date");
        TableColumn<Trip, String> typeCol = col("Type", "type");
        TableColumn<Trip, String> statusCol = col("Status", "status");
        TableColumn<Trip, String> airlineCol = col("Airline", "airlineName");

        TableColumn<Trip, String> purchaseCol = new TableColumn<>("Purchase");
        purchaseCol.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(String.valueOf(d.getValue().getPurchaseValue()))
        );

        TableColumn<Trip, String> sellCol = new TableColumn<>("Sell");
        sellCol.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(String.valueOf(d.getValue().getSellValue()))
        );

        TableColumn<Trip, String> profitCol = new TableColumn<>("Profit");
        profitCol.setCellValueFactory(d ->
                new javafx.beans.property.SimpleStringProperty(String.valueOf(d.getValue().getProfit()))
        );

        // Actions
        TableColumn<Trip, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setCellFactory(col -> new TableCell<>() {

            private final Button edit = new Button("✎");
            private final Button delete = new Button("🗑");
            private final HBox box = new HBox(10, edit, delete);

            {
                box.setAlignment(Pos.CENTER);

                edit.setOnAction(e -> {
                    Trip t = getTableView().getItems().get(getIndex());
                    showTripForm(root, t);
                });

                delete.setOnAction(e -> {
                    Trip t = getTableView().getItems().get(getIndex());
                    TripRepository.deleteTrip(t.getId());
                    showTripList(root);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        table.getColumns().addAll(
                idCol, clientCol, destCol, dateCol, typeCol,
                statusCol, airlineCol, purchaseCol, sellCol, profitCol, actionCol
        );

        // Pagination
        Button prev = new Button("‹ Prev");
        Button next = new Button("Next ›");
        Label pageInfo = new Label();

        HBox pagination = new HBox(10, prev, pageInfo, next);
        pagination.setAlignment(Pos.CENTER_RIGHT);

        Runnable refresh = () -> {

            List<Trip> allTrips = TripRepository.getAllTrips();

            List<Trip> filtered = allTrips.stream()
                    .filter(t -> search.getText().isEmpty()
                            || t.getClientName().toLowerCase().contains(search.getText().toLowerCase())
                            || t.getDestination().toLowerCase().contains(search.getText().toLowerCase()))
                    .filter(t -> dateFilter.getValue() == null
                            || t.getDate().equals(dateFilter.getValue().toString()))
                    .sorted(Comparator.comparing(t -> LocalDate.parse(t.getDate())))
                    .collect(Collectors.toList());

            int pageCount = Math.max(1, (int) Math.ceil((double) filtered.size() / PAGE_SIZE));

            if (currentPage >= pageCount) currentPage = pageCount - 1;
            if (currentPage < 0) currentPage = 0;

            int from = currentPage * PAGE_SIZE;
            int to = Math.min(from + PAGE_SIZE, filtered.size());

            table.setItems(FXCollections.observableArrayList(filtered.subList(from, to)));

            pageInfo.setText("Page " + (currentPage + 1) + " of " + pageCount);
        };

        search.textProperty().addListener((obs, o, n) -> {
            currentPage = 0;
            refresh.run();
        });

        dateFilter.valueProperty().addListener((obs, o, n) -> {
            currentPage = 0;
            refresh.run();
        });

        clearFilter.setOnAction(e -> {
            dateFilter.setValue(null);
            refresh.run();
        });

        prev.setOnAction(e -> {
            currentPage--;
            refresh.run();
        });

        next.setOnAction(e -> {
            currentPage++;
            refresh.run();
        });

        addBtn.setOnAction(e -> showTripForm(root, null));

        refresh.run();

        root.getChildren().addAll(header, search, table, pagination);
    }

    private static TableColumn<Trip, String> col(String title, String property) {
        TableColumn<Trip, String> col = new TableColumn<>(title);
        col.setCellValueFactory(new PropertyValueFactory<>(property));
        return col;
    }

    private static void showTripForm(VBox root, Trip editTrip) {
        root.getChildren().clear();

        boolean isEdit = editTrip != null;

        Label title = new Label(isEdit ? "Edit Trip" : "Add Trip");
        Button back = new Button("← Back");

        List<Client> clients = ClientRepository.getAllClients();

        ComboBox<String> clientBox = new ComboBox<>();  //drop box to show clients
        for (Client c : clients) {
            clientBox.getItems().add(c.getId() + " - " + c.getName());
        }

        TextField destination = new TextField();
        DatePicker date = new DatePicker();
        TextField type = new TextField();
        ComboBox<String> status = new ComboBox<>();
        status.getItems().addAll("Confirmed", "Pending");

        TextField airline = new TextField();
        TextField purchase = new TextField();
        TextField sell = new TextField();

        if (isEdit) {
            clientBox.setValue(editTrip.getClientId() + " - " + editTrip.getClientName());
            destination.setText(editTrip.getDestination());
            date.setValue(LocalDate.parse(editTrip.getDate()));
            type.setText(editTrip.getType());
            status.setValue(editTrip.getStatus());
            airline.setText(editTrip.getAirlineName());
            purchase.setText(String.valueOf(editTrip.getPurchaseValue()));
            sell.setText(String.valueOf(editTrip.getSellValue()));
        }

        Button save = new Button(isEdit ? "Update" : "Save");

        save.setOnAction(e -> {

            int clientId = Integer.parseInt(clientBox.getValue().split(" - ")[0]);

            String clientName = clients.stream()
                    .filter(c -> c.getId() == clientId)
                    .findFirst()
                    .map(Client::getName)
                    .orElse("");

            Trip t = new Trip(
                    clientId,
                    clientName,
                    destination.getText(),
                    date.getValue().toString(),
                    type.getText(),
                    status.getValue(),
                    parse(purchase.getText()),
                    parse(sell.getText()),
                    airline.getText()
            );

            if (isEdit) {
                TripRepository.updateTrip(editTrip.getId(), t);
            } else {
                TripRepository.addTrip(t);
            }

            showTripList(root);
        });

        back.setOnAction(e -> showTripList(root));

        VBox form = new VBox(10,
                title, clientBox, destination, date, type,
                status, airline, purchase, sell, save
        );

        root.getChildren().addAll(back, form);
    }

    private static double parse(String v) {
        try { return Double.parseDouble(v); }
        catch (Exception e) { return 0; }
    }

    public static void showTripDetailsScreen(VBox root, Trip trip) {

        root.getChildren().clear();

        Button backBtn = new Button("← Back");
        backBtn.getStyleClass().add("back-btn");

        Label title = new Label("Trip Details");
        title.getStyleClass().add("client-title");

        VBox card = new VBox(12);
        card.getStyleClass().add("panel");

        // 🔥 Fetch client from DB (not ClientData)
        Client client = ClientRepository.getAllClients()
                .stream()
                .filter(c -> c.getId() == trip.getClientId())
                .findFirst()
                .orElse(null);

        Label clientInfo = new Label(
                client == null ? "Client not found" :
                        "Name: " + client.getName() +
                        "\nPhone: " + client.getPhone() +
                        "\nEmail: " + client.getEmail() +
                        "\nCity: " + client.getCity()
        );

        Label tripInfo = new Label(
                "Destination: " + trip.getDestination() +
                        "\nDate: " + trip.getDate() +
                        "\nType: " + trip.getType() +
                        "\nStatus: " + trip.getStatus() +
                        "\nAirline: " + trip.getAirlineName()
        );

        Label payment = new Label(
                "Purchase: " + trip.getPurchaseValue() +
                        "\nSell: " + trip.getSellValue() +
                        "\nProfit: " + trip.getProfit()
        );

        card.getChildren().addAll(
                new Label("Client Details"), clientInfo,
                new Label("Trip Details"), tripInfo,
                new Label("Payment Details"), payment
        );

        backBtn.setOnAction(e -> showTripList(root));

        root.getChildren().addAll(backBtn, title, card);
    }
}