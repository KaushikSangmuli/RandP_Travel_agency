package com.agency.ui;

import com.agency.db.ClientRepository;
import com.agency.db.DocumentRepository;
import com.agency.db.TripRepository;
import com.agency.model.Client;
import com.agency.model.Document;
import com.agency.model.Trip;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.util.StringConverter;

import java.awt.Desktop;
import java.io.File;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import static com.agency.ui.DashboardScreen.loadIcon;

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
        dateFilter.getStyleClass().add("client-search");
        dateFilter.setPrefWidth(180);

        Button clearFilter = new Button("Clear");
        clearFilter.getStyleClass().add("back-btn");

        Button addBtn = new Button("Add New Trip");
        addBtn.setGraphic(loadIcon("plus.png",12));
        addBtn.getStyleClass().add("client-add-btn");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox header = new HBox(14, title, spacer, dateFilter, clearFilter, addBtn);
        header.setAlignment(Pos.CENTER_LEFT);

        TextField search = new TextField();
        search.setPromptText("Search trips...");
        search.getStyleClass().add("client-search");

        TableView<Trip> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPrefHeight(430);

        TableColumn<Trip, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(data -> {
            int rowIndex = data.getTableView().getItems().indexOf(data.getValue()) + 1;
            int displayIndex = currentPage * PAGE_SIZE + rowIndex;

            return new javafx.beans.property.SimpleStringProperty(
                    String.valueOf(displayIndex)
            );
        });
        idCol.setStyle("-fx-alignment: CENTER;");
        TableColumn<Trip, String> clientCol = col("Client", "clientName");
        TableColumn<Trip, String> destCol = col("Destination", "destination");
        TableColumn<Trip, String> dateCol = col("Date", "date");
        TableColumn<Trip, String> typeCol = col("Type", "type");
        TableColumn<Trip, String> statusCol = col("Status", "status");
        TableColumn<Trip, String> airlineCol = col("Airline", "airlineName");

        statusCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);

                if (empty || status == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }

                Label pill = new Label(status);
                pill.getStyleClass().add("status-pill");

                if (status.equalsIgnoreCase("Confirmed")) {
                    pill.getStyleClass().add("status-confirmed");
                } else if (status.equalsIgnoreCase("Cancelled")) {
                    pill.getStyleClass().add("status-cancelled");
                } else {
                    pill.getStyleClass().add("status-pending");
                }

                setGraphic(pill);
                setText(null);
                setAlignment(Pos.CENTER);
            }
        });

        TableColumn<Trip, String> purchaseCol = new TableColumn<>("Purchase");
        purchaseCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        String.valueOf(data.getValue().getPurchaseValue())
                )
        );
        purchaseCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<Trip, String> sellCol = new TableColumn<>("Sell");
        sellCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        String.valueOf(data.getValue().getSellValue())
                )
        );
        sellCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<Trip, String> profitCol = new TableColumn<>("Profit");
        profitCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        String.valueOf(data.getValue().getProfit())
                )
        );
        profitCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<Trip, Void> detailsCol = new TableColumn<>("Details");
        detailsCol.setCellFactory(param -> new TableCell<>() {
            private final Button view = new Button("View");

            {
                view.getStyleClass().add("view-details-btn");
                view.setOnAction(e -> {
                    Trip trip = getTableView().getItems().get(getIndex());
                    showTripDetailsScreen(root, trip);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : view);
                setAlignment(Pos.CENTER);
            }
        });

        TableColumn<Trip, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setCellFactory(param -> new TableCell<>() {
            private final Button edit = createIconButton("/icons/edit.png", "", "edit-btn");
            private final Button delete = createIconButton("/icons/delete.png", "", "delete-btn");
            private final HBox box = new HBox(8, edit, delete);

            {
                edit.getStyleClass().add("edit-btn");
                delete.getStyleClass().add("delete-btn");
                box.setAlignment(Pos.CENTER);

                edit.setOnAction(e -> {
                    Trip trip = getTableView().getItems().get(getIndex());
                    showTripForm(root, trip);
                });

                delete.setOnAction(e -> {
                    Trip trip = getTableView().getItems().get(getIndex());

                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Delete Trip");
                    confirm.setHeaderText("Are you sure you want to delete this trip?");
                    confirm.setContentText("Trip: " + trip.getClientName() + " - " + trip.getDestination());

                    confirm.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.OK) {
                            TripRepository.deleteTrip(trip.getId());
                            showTripList(root);
                        }
                    });
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
                setAlignment(Pos.CENTER);
            }
        });

        table.getColumns().addAll(
                idCol, clientCol, destCol, dateCol, typeCol, statusCol,
                airlineCol, purchaseCol, sellCol, profitCol, detailsCol, actionsCol
        );

        Button prev = new Button("Prev",loadIcon("left.png",10));
        Button next = new Button("Next",loadIcon("right.png",10));
        next.setContentDisplay(ContentDisplay.RIGHT);
        Label pageInfo = new Label();

        prev.getStyleClass().add("back-btn");
        next.getStyleClass().add("back-btn");

        HBox pagination = new HBox(12, prev, pageInfo, next);
        pagination.setAlignment(Pos.CENTER_RIGHT);

        Runnable refresh = () -> {
            List<Trip> filtered = getFilteredTrips(search.getText(), dateFilter.getValue());

            int pageCount = Math.max(1, (int) Math.ceil((double) filtered.size() / PAGE_SIZE));

            if (currentPage >= pageCount) currentPage = pageCount - 1;
            if (currentPage < 0) currentPage = 0;

            int from = currentPage * PAGE_SIZE;
            int to = Math.min(from + PAGE_SIZE, filtered.size());

            table.setItems(FXCollections.observableArrayList(filtered.subList(from, to)));

            pageInfo.setText("Page " + (currentPage + 1) + " of " + pageCount);
            prev.setDisable(currentPage == 0);
            next.setDisable(currentPage >= pageCount - 1);
        };

        search.textProperty().addListener((obs, oldVal, newVal) -> {
            currentPage = 0;
            refresh.run();
        });

        dateFilter.valueProperty().addListener((obs, oldVal, newVal) -> {
            currentPage = 0;
            refresh.run();
        });

        clearFilter.setOnAction(e -> {
            dateFilter.setValue(null);
            currentPage = 0;
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

        refresh.run();

        VBox panel = new VBox(14, table, pagination);
        panel.getStyleClass().add("panel");

        addBtn.setOnAction(e -> showTripForm(root, null));

        root.getChildren().addAll(header, search, panel);
    }

    private static TableColumn<Trip, String> col(String title, String property) {
        TableColumn<Trip, String> col = new TableColumn<>(title);
        col.setCellValueFactory(new PropertyValueFactory<>(property));
        col.setStyle("-fx-alignment: CENTER;");
        return col;
    }

    private static List<Trip> getFilteredTrips(String text, LocalDate selectedDate) {
        String keyword = text == null ? "" : text.toLowerCase().trim();
        String date = selectedDate == null ? "" : selectedDate.toString();

        return TripRepository.getAllTrips().stream()
                .filter(t -> t.getDate() != null && !t.getDate().isEmpty())
                .filter(t -> keyword.isEmpty()
                        || safe(t.getClientName()).toLowerCase().contains(keyword)
                        || safe(t.getDestination()).toLowerCase().contains(keyword)
                        || safe(t.getDate()).toLowerCase().contains(keyword)
                        || safe(t.getType()).toLowerCase().contains(keyword)
                        || safe(t.getStatus()).toLowerCase().contains(keyword)
                        || safe(t.getAirlineName()).toLowerCase().contains(keyword))
                .filter(t -> date.isEmpty() || t.getDate().equals(date))
                .sorted(Comparator.comparing(t -> LocalDate.parse(t.getDate())))
                .collect(Collectors.toList());
    }

    public static void showTripDetailsScreen(VBox root, Trip trip) {
        root.getChildren().clear();

        Client client = getClientById(trip.getClientId());

        Button backBtn = new Button("Back",loadIcon("backArrow.png",12));
        backBtn.getStyleClass().add("back-btn");

        Label title = new Label("Trip Details");
        title.getStyleClass().add("client-title");

        HBox header = new HBox(14, backBtn, title);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox card = new VBox(14);
        card.getStyleClass().add("panel");

        Label section1 = new Label("Client Details");
        section1.getStyleClass().add("section-title");

        Label clientInfo = new Label(
                client == null ? "Client not found" :
                        "Name: " + safe(client.getName()) +
                        "\nPhone: " + safe(client.getPhone()) +
                        "\nEmail: " + safe(client.getEmail()) +
                        "\nCity: " + safe(client.getCity())
        );

        Label section2 = new Label("Trip Details");
        section2.getStyleClass().add("section-title");

        Label tripInfo = new Label(
                "Destination: " + safe(trip.getDestination()) +
                        "\nDate: " + safe(trip.getDate()) +
                        "\nType: " + safe(trip.getType()) +
                        "\nStatus: " + safe(trip.getStatus()) +
                        "\nAirline: " + safe(trip.getAirlineName())
        );

        Label section3 = new Label("Payment Details");
        section3.getStyleClass().add("section-title");

        Label paymentInfo = new Label(
                "Purchase: " + trip.getPurchaseValue() +
                        "\nSell: " + trip.getSellValue() +
                        "\nService Fee: " + trip.getServiceFee() +
                        "\nProfit: " + trip.getProfit()
        );

        Label section4 = new Label("Documents");
        section4.getStyleClass().add("section-title");

        VBox docsBox = new VBox(8);
        List<Document> documents = DocumentRepository.getDocumentsByTrip(trip.getId());

        if (documents.isEmpty()) {
            docsBox.getChildren().add(new Label("No documents uploaded yet"));
        } else {
            for (Document doc : documents) {
                File file = new File(doc.getFilePath());

                Label fileName = new Label(file.getName());

                Button viewBtn = new Button("View");
                viewBtn.getStyleClass().add("view-details-btn");

                viewBtn.setOnAction(e -> {
                    try {
                        Desktop.getDesktop().open(file);
                    } catch (Exception ex) {
                        alert("Cannot open file");
                    }
                });

                HBox row = new HBox(10, fileName, viewBtn);
                row.setAlignment(Pos.CENTER_LEFT);
                docsBox.getChildren().add(row);
            }
        }

        card.getChildren().addAll(
                section1, clientInfo,
                section2, tripInfo,
                section3, paymentInfo,
                section4, docsBox
        );

        backBtn.setOnAction(e -> showTripList(root));

        root.getChildren().addAll(header, card);
    }

    private static Client getClientById(int clientId) {
        return ClientRepository.getAllClients()
                .stream()
                .filter(c -> c.getId() == clientId)
                .findFirst()
                .orElse(null);
    }

    private static void showTripForm(VBox root, Trip editTrip) {
        root.getChildren().clear();

        boolean isEdit = editTrip != null;

        Button backBtn = new Button("Back",loadIcon("backArrow.png",12));
        backBtn.getStyleClass().add("back-btn");

        Label title = new Label(isEdit ? "Edit Trip" : "Add New Trip");
        title.getStyleClass().add("client-title");

        HBox header = new HBox(14, backBtn, title);
        header.setAlignment(Pos.CENTER_LEFT);

        GridPane form = new GridPane();
        form.setHgap(20);
        form.setVgap(18);
        form.getStyleClass().add("client-form-card");

        TextField clientName = input("Client name");
        TextField clientPhone = input("Client phone");
        TextField clientEmail = input("Client email");
        TextField clientCity = input("Client city");

        clientName.setEditable(false);
        clientPhone.setEditable(false);
        clientEmail.setEditable(false);
        clientCity.setEditable(false);

        TextField clientSearch = input("Search client by name / phone / email");

        ListView<Client> clientList = new ListView<>();
        clientList.setPrefHeight(140);
        clientList.setMinHeight(140);
        clientList.setMaxHeight(140);
        clientList.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #d6e0ee;" +
                        "-fx-border-radius: 10;" +
                        "-fx-background-radius: 10;"
        );

        clientList.setVisible(false);
        clientList.setManaged(false);

        List<Client> clients = ClientRepository.getAllClients();
        clientList.setItems(FXCollections.observableArrayList(clients));

        final Client[] selectedClientHolder = {null};
        final boolean[] selectingClient = {false};

        clientList.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Client client, boolean empty) {
                super.updateItem(client, empty);

                if (empty || client == null) {
                    setText(null);
                } else {
                    setText(client.getId() + " - " + safe(client.getName()) + " - " + safe(client.getPhone()));
                }
            }
        });

        clientSearch.textProperty().addListener((obs, oldVal, newVal) -> {
            if (selectingClient[0]) return;

            selectedClientHolder[0] = null;
            clientName.clear();
            clientPhone.clear();
            clientEmail.clear();
            clientCity.clear();

            String keyword = newVal == null ? "" : newVal.toLowerCase().trim();

            if (keyword.isEmpty()) {
                clientList.setItems(FXCollections.observableArrayList());
                clientList.setVisible(false);
                clientList.setManaged(false);
                return;
            }

            List<Client> filteredClients = clients.stream()
                    .filter(c ->
                            String.valueOf(c.getId()).contains(keyword)
                                    || safe(c.getName()).toLowerCase().contains(keyword)
                                    || safe(c.getPhone()).toLowerCase().contains(keyword)
                                    || safe(c.getEmail()).toLowerCase().contains(keyword)
                                    || safe(c.getCity()).toLowerCase().contains(keyword)
                    )
                    .collect(Collectors.toList());

            clientList.setItems(FXCollections.observableArrayList(filteredClients));

            boolean showList = !filteredClients.isEmpty();
            clientList.setVisible(showList);
            clientList.setManaged(showList);
        });

        clientList.setOnMouseClicked(e -> {
            Client c = clientList.getSelectionModel().getSelectedItem();

            if (c == null) return;

            selectedClientHolder[0] = c;

            selectingClient[0] = true;

            clientSearch.setText(c.getId() + " - " + safe(c.getName()) + " - " + safe(c.getPhone()));

            clientName.setText(safe(c.getName()));
            clientPhone.setText(safe(c.getPhone()));
            clientEmail.setText(safe(c.getEmail()));
            clientCity.setText(safe(c.getCity()));
            clientList.setVisible(false);
            clientList.setManaged(false);

            selectingClient[0] = false;
        });

        TextField destination = input("Enter destination");

        DatePicker date = new DatePicker();
        date.getStyleClass().add("client-input");
        date.setPromptText("Select date");
        date.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                setDisable(empty || item.isBefore(LocalDate.now()));
            }
        });

        TextField type = input("Leisure / Business / Honeymoon");

        ComboBox<String> status = new ComboBox<>();
        status.getItems().addAll("Confirmed", "Pending", "Cancelled");
        status.setPromptText("Select status");
        status.getStyleClass().add("client-input");

        TextField airline = input("Enter airline name");
        TextField purchase = input("Enter purchase value");
        TextField sell = input("Enter sell value");
        TextField serviceFee = input("Enter Service amount");

        if (isEdit) {
            Client selectedClient = getClientById(editTrip.getClientId());

            if (selectedClient != null) {
                selectedClientHolder[0] = selectedClient;

                selectingClient[0] = true;

                clientSearch.setText(
                        selectedClient.getId() + " - " + safe(selectedClient.getName()) + " - " + safe(selectedClient.getPhone())
                );

                clientName.setText(safe(selectedClient.getName()));
                clientPhone.setText(safe(selectedClient.getPhone()));
                clientEmail.setText(safe(selectedClient.getEmail()));
                clientCity.setText(safe(selectedClient.getCity()));

                selectingClient[0] = false;
            }

            destination.setText(safe(editTrip.getDestination()));

            if (editTrip.getDate() != null && !editTrip.getDate().isEmpty()) {
                date.setValue(LocalDate.parse(editTrip.getDate()));
            }

            type.setText(safe(editTrip.getType()));
            status.setValue(editTrip.getStatus());
            airline.setText(safe(editTrip.getAirlineName()));
            purchase.setText(String.valueOf(editTrip.getPurchaseValue()));
            sell.setText(String.valueOf(editTrip.getSellValue()));
            serviceFee.setText(String.valueOf(editTrip.getServiceFee()));
        }

        form.add(label("Client ID"), 0, 0);
        VBox clientSearchBox = new VBox(8, clientSearch, clientList);
        form.add(clientSearchBox, 0, 1);
        form.add(label("Client Name"), 1, 0);
        form.add(clientName, 1, 1);

        form.add(label("Phone"), 0, 2);
        form.add(clientPhone, 0, 3);
        form.add(label("Email"), 1, 2);
        form.add(clientEmail, 1, 3);

        form.add(label("City"), 0, 4);
        form.add(clientCity, 0, 5);
        form.add(label("Destination"), 1, 4);
        form.add(destination, 1, 5);

        form.add(label("Date"), 0, 6);
        form.add(date, 0, 7);
        form.add(label("Trip Type"), 1, 6);
        form.add(type, 1, 7);

        form.add(label("Status"), 0, 8);
        form.add(status, 0, 9);
        form.add(label("Airline Name"), 1, 8);
        form.add(airline, 1, 9);

        form.add(label("Purchase Value"), 0, 10);
        form.add(purchase, 0, 11);
        form.add(label("Sell Value"), 1, 10);
        form.add(sell, 1, 11);

        form.add(label("Service Fee"), 0, 12);
        form.add(serviceFee, 0, 13);

        Button save = new Button(isEdit ? "Update Trip" : "Save Trip");
        save.getStyleClass().add("client-add-btn");

        save.setOnAction(e -> {
            Client selectedClient = selectedClientHolder[0];

            if (selectedClient == null) {
                alert("Please select a client from the dropdown");
                return;
            }

            if (destination.getText().trim().isEmpty()) {
                alert("Destination is required");
                return;
            }

            if (date.getValue() == null) {
                alert("Please select trip date");
                return;
            }

            if (date.getValue().isBefore(LocalDate.now())) {
                alert("Trip date cannot be before today");
                return;
            }

            if (status.getValue() == null) {
                alert("Please select status");
                return;
            }

            Trip newTrip = new Trip(
                    selectedClient.getId(),
                    safe(selectedClient.getName()),
                    destination.getText().trim(),
                    date.getValue().toString(),
                    type.getText().trim(),
                    status.getValue(),
                    parseAmount(purchase.getText()),
                    parseAmount(sell.getText()),
                    airline.getText().trim(),
                    parseAmount(serviceFee.getText())
            );

            if (isEdit) {
                newTrip.setId(editTrip.getId());
                TripRepository.updateTrip(editTrip.getId(), newTrip);
            } else {
                TripRepository.addTrip(newTrip);
            }

            showTripList(root);
        });

        backBtn.setOnAction(e -> showTripList(root));

        VBox formContainer = new VBox(20, header, form, save);
        formContainer.setFillWidth(true);

        ScrollPane scroll = new ScrollPane(formContainer);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);
        scroll.setPannable(true);

        root.getChildren().add(scroll);
    }

    private static double parseAmount(String value) {
        try {
            return Double.parseDouble(value.trim());
        } catch (Exception e) {
            return 0;
        }
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static void alert(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setContentText(msg);
        alert.show();
    }

    private static TextField input(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.getStyleClass().add("client-input");
        return field;
    }

    private static Label label(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("form-label");
        return label;
    }
    private static Button createIconButton(String iconPath, String fallbackText, String styleClass) {
        Node graphic = null;

        try {
            ImageView icon = new ImageView(new Image(ClientsScreen.class.getResource(iconPath).toExternalForm()));
            icon.setFitWidth(16);
            icon.setFitHeight(16);
            graphic = icon;
        } catch (Exception ignored) {}

        Button btn = graphic == null ? new Button(fallbackText) : new Button("", graphic);
        btn.getStyleClass().add(styleClass);
        return btn;
    }
}