package com.agency.ui;

import com.agency.model.Client;
import com.agency.db.ClientRepository;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;

public class ClientsScreen {

    private static final int PAGE_SIZE = 8;

    // 🔥 SOURCE DATA (DB ↔ UI bridge)
    private static final ObservableList<Client> masterData =
            FXCollections.observableArrayList();

    // 🔥 FILTER WRAPPER
    private static final FilteredList<Client> filteredClients =
            new FilteredList<>(masterData, p -> true);

    public static VBox getView() {
        VBox root = new VBox(20);
        root.getStyleClass().add("client-root");

        loadFromDB();   // initial load
        showClientList(root);

        return root;
    }

    private static void loadFromDB() {
        List<Client> freshData = ClientRepository.getAllClients();
        masterData.setAll(freshData);   // ✅ ONLY CORRECT WAY
    }

    private static void showClientList(VBox root) {
        root.getChildren().clear();

        Label title = new Label("Clients");
        title.getStyleClass().add("client-title");

        Button addBtn = new Button("+ Add New Client");
        addBtn.getStyleClass().add("client-add-btn");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox header = new HBox(title, spacer, addBtn);
        header.setAlignment(Pos.CENTER_LEFT);

        TextField search = new TextField();
        search.setPromptText("Search clients...");
        search.getStyleClass().add("client-search");

        TableView<Client> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Client, Number> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(c ->
                new SimpleIntegerProperty(c.getValue().getId())
        );

        TableColumn<Client, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(c.getValue().getName()));

        TableColumn<Client, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(c.getValue().getPhone()));

        TableColumn<Client, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(c.getValue().getEmail()));

        TableColumn<Client, String> cityCol = new TableColumn<>("City");
        cityCol.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(c.getValue().getCity()));

        TableColumn<Client, Void> actionCol = new TableColumn<>("Actions");

        actionCol.setCellFactory(col -> new TableCell<>() {

            private final Button editBtn = new Button("✎");
            private final Button deleteBtn = new Button("🗑");
            private final HBox box = new HBox(12, editBtn, deleteBtn);

            {
                box.setAlignment(Pos.CENTER);

                editBtn.setOnAction(e -> {
                    Client client = getTableView().getItems().get(getIndex());
                    showClientForm(root, client);
                });

                deleteBtn.setOnAction(e -> {
                    Client client = getTableView().getItems().get(getIndex());

                    ClientRepository.deleteClient(client.getId());
                    loadFromDB();     // 🔥 refresh data
                    table.refresh();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        table.getColumns().addAll(idCol, nameCol, phoneCol, emailCol, cityCol, actionCol);

        // 🔥 FIXED TABLE BINDING
        table.setItems(filteredClients);

        // pagination
        Button prev = new Button("‹ Prev");
        Button next = new Button("Next ›");
        Label pageInfo = new Label();

        HBox pagination = new HBox(12, prev, pageInfo, next);
        pagination.setAlignment(Pos.CENTER_RIGHT);

        final int[] currentPage = {0};

        Runnable refresh = () -> {

            int total = filteredClients.size();
            int pageCount = Math.max(1, (int) Math.ceil((double) total / PAGE_SIZE));

            if (currentPage[0] >= pageCount) currentPage[0] = pageCount - 1;
            if (currentPage[0] < 0) currentPage[0] = 0;

            int from = currentPage[0] * PAGE_SIZE;
            int to = Math.min(from + PAGE_SIZE, total);

            table.setItems(FXCollections.observableArrayList(
                    filteredClients.subList(from, to)
            ));

            pageInfo.setText("Page " + (currentPage[0] + 1) + " of " + pageCount);
            prev.setDisable(currentPage[0] == 0);
            next.setDisable(currentPage[0] >= pageCount - 1);
        };

        search.textProperty().addListener((obs, oldVal, newVal) -> {
            String keyword = newVal.toLowerCase().trim();

            filteredClients.setPredicate(c -> {
                if (keyword.isEmpty()) return true;

                return c.getName().toLowerCase().contains(keyword)
                        || c.getPhone().toLowerCase().contains(keyword)
                        || c.getEmail().toLowerCase().contains(keyword)
                        || c.getCity().toLowerCase().contains(keyword);
            });

            currentPage[0] = 0;
            refresh.run();
        });

        prev.setOnAction(e -> {
            currentPage[0]--;
            refresh.run();
        });

        next.setOnAction(e -> {
            currentPage[0]++;
            refresh.run();
        });

        addBtn.setOnAction(e -> showClientForm(root, null));

        refresh.run();

        root.getChildren().addAll(header, search, table, pagination);
    }

    private static void showClientForm(VBox root, Client editClient) {
        root.getChildren().clear();

        boolean isEdit = editClient != null;

        Label title = new Label(isEdit ? "Edit Client" : "Add Client");

        TextField name = new TextField();
        TextField phone = new TextField();
        TextField email = new TextField();
        TextField city = new TextField();

        if (isEdit) {
            name.setText(editClient.getName());
            phone.setText(editClient.getPhone());
            email.setText(editClient.getEmail());
            city.setText(editClient.getCity());
        }

        Button save = new Button(isEdit ? "Update" : "Save");
        Button back = new Button("← Back");

        save.setOnAction(e -> {

            Client c = new Client(
                    isEdit ? editClient.getId() : 0,
                    name.getText(),
                    phone.getText(),
                    email.getText(),
                    city.getText()
            );

            if (isEdit) {
                ClientRepository.updateClient(c);
            } else {
                ClientRepository.addClient(c);
            }

            loadFromDB(); // 🔥 sync again from DB
            showClientList(root);
        });

        back.setOnAction(e -> showClientList(root));

        VBox form = new VBox(12, title, name, phone, email, city, save);
        root.getChildren().addAll(back, form);
    }
}