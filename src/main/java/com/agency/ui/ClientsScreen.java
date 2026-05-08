package com.agency.ui;

import com.agency.cache.AppCache;
import com.agency.db.ClientRepository;
import com.agency.db.DocumentRepository;
import com.agency.db.TripRepository;
import com.agency.model.Client;
import com.agency.model.Document;
import com.agency.model.Trip;
import com.agency.util.AppLogger;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.agency.ui.DashboardScreen.loadIcon;

public class ClientsScreen {

    private static final int PAGE_SIZE = 8;

    public static VBox getView() {
        VBox root = new VBox(20);
        root.getStyleClass().add("client-root");
        showClientList(root);
        return root;
    }

    public static VBox getAddClientViewDirect() {
        VBox root = new VBox(22);
        root.getStyleClass().add("client-root");
        showClientForm(root, null);
        return root;
    }

    private static void showClientList(VBox root) {
        root.getChildren().clear();

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Clients");
        title.getStyleClass().add("client-title");
        Button addBtn = new Button("Add New Client",loadIcon("plus.png",12));
        addBtn.getStyleClass().add("client-add-btn");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().addAll(title, spacer, addBtn);

        TextField search = new TextField();
        search.setPromptText("Search clients...");
        search.getStyleClass().add("client-search");

        ObservableList<Client> clientList =
                FXCollections.observableArrayList(AppCache.getClients());

        FilteredList<Client> filteredClients =
                new FilteredList<>(clientList, p -> true);

        TableView<Client> table = new TableView<>();
        table.getStyleClass().add("client-table");
        table.setPlaceholder(new Label("No clients available"));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Client, Number> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(c -> new SimpleIntegerProperty(filteredClients.indexOf(c.getValue()) + 1));

        TableColumn<Client, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Client, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));

        TableColumn<Client, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));

        TableColumn<Client, String> cityCol = new TableColumn<>("City");
        cityCol.setCellValueFactory(new PropertyValueFactory<>("city"));

        TableColumn<Client, Void> detailsCol = new TableColumn<>("Details");

        detailsCol.setCellFactory(col -> new TableCell<>() {
            private final Button viewBtn = new Button("View");
            private final HBox box = new HBox(viewBtn);

            {
                viewBtn.getStyleClass().add("view-details-btn");
                box.setAlignment(Pos.CENTER);

                viewBtn.setOnAction(e -> {
                    Client client = getTableView().getItems().get(getIndex());
                    showClientDetails(root, client);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        TableColumn<Client, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = createIconButton("/icons/edit.png", "", "edit-btn");
            private final Button deleteBtn = createIconButton("/icons/delete.png", "", "delete-btn");
            private final HBox box = new HBox(12, editBtn, deleteBtn);

            {
                box.setAlignment(Pos.CENTER);

                editBtn.setOnAction(e -> {
                    Client client = getTableView().getItems().get(getIndex());
                    showClientForm(root, client);
                });

                deleteBtn.setOnAction(e -> {
                    Client client = getTableView().getItems().get(getIndex());

                    boolean hasTrips = AppCache.getTrips().stream()
                            .anyMatch(t -> client.getUuid().equals(t.getClientUuid()));

                    if (hasTrips) {
                        alert("This client has linked trips. Please delete the trips first or keep the client record for history.");
                        return;
                    }

                    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                    confirm.setTitle("Delete Client");
                    confirm.setHeaderText("Are you sure you want to delete this client?");
                    confirm.setContentText("Client: " + client.getName());

                    confirm.showAndWait().ifPresent(response -> {
                        if (response == ButtonType.OK) {
                            ClientRepository.deleteClient(client.getUuid());
                            AppCache.reloadClients();
                            showClientList(root);
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

        table.getColumns().addAll(idCol, nameCol, phoneCol, emailCol, cityCol, detailsCol, actionCol);

        Button prevBtn = new Button("Prev",loadIcon("left.png",10));
        Button nextBtn = new Button("Next",loadIcon("right.png",10));
        nextBtn.setContentDisplay(ContentDisplay.RIGHT);
        Label pageInfo = new Label();

        prevBtn.getStyleClass().add("page-btn");
        nextBtn.getStyleClass().add("page-btn");
        pageInfo.getStyleClass().add("page-info");

        HBox pagination = new HBox(12, prevBtn, pageInfo, nextBtn);
        pagination.setAlignment(Pos.CENTER_RIGHT);
        pagination.getStyleClass().add("pagination-box");

        int[] currentPage = {0};

        Runnable updateTable = () -> {
            int total = filteredClients.size();
            int pageCount = Math.max(1, (int) Math.ceil((double) total / PAGE_SIZE));

            if (currentPage[0] >= pageCount) currentPage[0] = pageCount - 1;
            if (currentPage[0] < 0) currentPage[0] = 0;

            int from = currentPage[0] * PAGE_SIZE;
            int to = Math.min(from + PAGE_SIZE, total);

            table.setItems(FXCollections.observableArrayList(filteredClients.subList(from, to)));

            pageInfo.setText("Page " + (currentPage[0] + 1) + " of " + pageCount);
            prevBtn.setDisable(currentPage[0] == 0);
            nextBtn.setDisable(currentPage[0] >= pageCount - 1);
        };

        search.textProperty().addListener((obs, oldVal, newVal) -> {
            String keyword = newVal == null ? "" : newVal.toLowerCase().trim();

            filteredClients.setPredicate(client -> {
                if (keyword.isEmpty()) return true;

                return safe(client.getName()).toLowerCase().contains(keyword)
                        || safe(client.getPhone()).toLowerCase().contains(keyword)
                        || safe(client.getEmail()).toLowerCase().contains(keyword)
                        || safe(client.getCity()).toLowerCase().contains(keyword);
            });

            currentPage[0] = 0;
            updateTable.run();
        });

        prevBtn.setOnAction(e -> {
            currentPage[0]--;
            updateTable.run();
        });

        nextBtn.setOnAction(e -> {
            currentPage[0]++;
            updateTable.run();
        });

        addBtn.setOnAction(e -> showClientForm(root, null));

        updateTable.run();

        VBox tableWrapper = new VBox(table);
        VBox.setVgrow(table, Priority.ALWAYS);
        VBox.setVgrow(tableWrapper, Priority.ALWAYS);

        root.getChildren().addAll(header, search, tableWrapper, pagination);
    }

    private static void showClientDetails(VBox root, Client client) {
        root.getChildren().clear();

        Button backBtn = new Button("Back",loadIcon("backArrow.png",12));
        backBtn.getStyleClass().add("back-btn");

        Label title = new Label("Client Details");
        title.getStyleClass().add("client-title");

        HBox header = new HBox(14, backBtn, title);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox card = new VBox(14);
        card.getStyleClass().add("panel");

        Label clientTitle = new Label("Client Information");
        clientTitle.getStyleClass().add("section-title");

        Label clientInfo = new Label(
                "Name: " + safe(client.getName()) +
                        "\nPhone: " + safe(client.getPhone()) +
                        "\nEmail: " + safe(client.getEmail()) +
                        "\nCity: " + safe(client.getCity())
        );

        Label tripsTitle = new Label("Linked Trips");
        tripsTitle.getStyleClass().add("section-title");

        VBox tripsBox = new VBox(8);

        double totalPurchase = 0;
        double totalSell = 0;
        double totalProfit = 0;
        int tripCount = 0;

        List<Trip> trips = AppCache.getTrips();

        for (Trip trip : trips) {
            if (trip.getClientId() == client.getId()) {
                tripCount++;
                totalPurchase += trip.getPurchaseValue();
                totalSell += trip.getSellValue();
                totalProfit += trip.getProfit();

                tripsBox.getChildren().add(new Label(
                        safe(trip.getDate()) + " | " +
                                safe(trip.getDestination()) + " | " +
                                safe(trip.getStatus()) + " | Profit: " + trip.getProfit()
                ));
            }
        }

        if (tripCount == 0) {
            tripsBox.getChildren().add(new Label("No trips found for this client."));
        }

        Label totalsTitle = new Label("Business Summary");
        totalsTitle.getStyleClass().add("section-title");

        Label totals = new Label(
                "Total Trips: " + tripCount +
                        "\nTotal Purchase: " + totalPurchase +
                        "\nTotal Sell: " + totalSell +
                        "\nTotal Profit: " + totalProfit
        );

        Label docsTitle = new Label("Documents");
        docsTitle.getStyleClass().add("section-title");

        VBox docsBox = new VBox(8);

        List<Document> allDocs = new ArrayList<>();



        for (Trip t : trips) {
            if (client.getUuid().equals(t.getClientUuid())) {
                AppCache.getDocuments().stream()
                        .filter(d -> t.getUuid().equals(d.getTripUuid()))
                        .forEach(allDocs::add);
            }
        }

        if (allDocs.isEmpty()) {
            docsBox.getChildren().add(new Label("No documents uploaded yet."));
        } else {
            for (Document doc : allDocs) {

                File file = new File(doc.getFilePath());

                Label fileLabel = new Label(file.getName());

                Button view = new Button("View");
                view.getStyleClass().add("view-details-btn");

                view.setOnAction(e -> {
                    try {
                        Desktop.getDesktop().open(file);
                    } catch (Exception ex) {
                        AppLogger.logError(ex, "Failed at Show Client Details." );
                        alert("Cannot open file");
                    }
                });

                HBox row = new HBox(10, fileLabel, view);
                row.setAlignment(Pos.CENTER_LEFT);

                docsBox.getChildren().add(row);
            }
        }
        card.getChildren().addAll(
                clientTitle, clientInfo,
                tripsTitle, tripsBox,
                totalsTitle, totals,
                docsTitle, docsBox
        );

        backBtn.setOnAction(e -> showClientList(root));

        root.getChildren().addAll(header, card);
    }

    private static void showClientForm(VBox root, Client editClient) {
        root.getChildren().clear();

        boolean isEdit = editClient != null;

        Button backBtn = new Button("Back",loadIcon("backArrow.png",12));
        backBtn.getStyleClass().add("back-btn");

        Label title = new Label(isEdit ? "Edit Client" : "Add New Client");
        title.getStyleClass().add("client-title");

        HBox header = new HBox(14, backBtn, title);
        header.setAlignment(Pos.CENTER_LEFT);

        GridPane form = new GridPane();
        form.setHgap(20);
        form.setVgap(18);
        form.getStyleClass().add("client-form-card");

        TextField name = input("Enter full name");
        TextField phone = input("Enter phone number");
        TextField email = input("Enter email address");
        TextField city = input("Enter city");

        if (isEdit) {
            name.setText(editClient.getName());
            phone.setText(editClient.getPhone());
            email.setText(editClient.getEmail());
            city.setText(editClient.getCity());
        }

        form.add(label("Name"), 0, 0);
        form.add(name, 0, 1);
        form.add(label("Phone"), 1, 0);
        form.add(phone, 1, 1);
        form.add(label("Email"), 0, 2);
        form.add(email, 0, 3);
        form.add(label("City"), 1, 2);
        form.add(city, 1, 3);

        Button save = new Button(isEdit ? "Update Client" : "Save Client");
        save.getStyleClass().add("client-add-btn");

        save.setOnAction(e -> {
            if (name.getText().trim().isEmpty()) {
                alert("Client name is required");
                return;
            }

            if (phone.getText().trim().isEmpty()) {
                alert("Mobile number is required");
                return;
            }

            Client client = new Client(
                    isEdit ? editClient.getId() : 0,
                    name.getText().trim(),
                    phone.getText().trim(),
                    email.getText().trim(),
                    city.getText().trim()
            );

            if (isEdit) {
                client.setUuid(editClient.getUuid());
                ClientRepository.updateClient(client);
                AppCache.reloadClients();
            } else {
                ClientRepository.addClient(client);
                AppCache.reloadClients();
            }

            showClientList(root);
        });

        backBtn.setOnAction(e -> showClientList(root));

        root.getChildren().addAll(header, form, save);
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

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static void alert(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setContentText(msg);
        alert.show();
    }
}