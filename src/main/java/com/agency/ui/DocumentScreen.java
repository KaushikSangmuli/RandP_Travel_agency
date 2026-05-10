package com.agency.ui;

import com.agency.cache.AppCache;
import com.agency.db.DocumentRepository;
import com.agency.model.Document;
import com.agency.model.Trip;
import com.agency.util.AppLogger;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;

import java.awt.*;
import java.io.File;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.agency.ui.DashboardScreen.loadIcon;
import static com.agency.ui.SettingsScreen.popup;

public class DocumentScreen {

    private static VBox root;
    private static TextField tripSearch;
    private static ListView<Trip> tripList;
    private static VBox listBox;
    private static Trip selectedTrip;

    // ✅ NEW
    private static ComboBox<String> category;
    private static ComboBox<String> subType;

    public static VBox getView() {
        root = new VBox(22);
        root.getStyleClass().add("client-root");
        buildUI();
        return root;
    }

    private static void buildUI() {

        Label title = new Label("Documents");
        title.getStyleClass().add("client-title");

        tripSearch = new TextField();
        tripSearch.setPromptText("Search trip by client / destination / date");
        tripSearch.getStyleClass().add("client-input");

        tripList = new ListView<>();
        tripList.setPrefHeight(140);
        tripList.setMinHeight(140);
        tripList.setMaxHeight(140);
        tripList.setVisible(false);
        tripList.setManaged(false);

        List<Trip> trips = AppCache.getTrips();

        tripList.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Trip t, boolean empty) {
                super.updateItem(t, empty);
                if (empty || t == null) {
                    setText(null);
                } else {
                    setText(t.getId() + " - " + safe(t.getClientName()) + " - " + safe(t.getDestination()));
                }
            }
        });

        final boolean[] selectingTrip = {false};

        tripSearch.textProperty().addListener((obs, oldVal, newVal) -> {
            if (selectingTrip[0]) return;

            selectedTrip = null;

            String keyword = newVal == null ? "" : newVal.toLowerCase().trim();

            if (keyword.isEmpty()) {
                tripList.setItems(FXCollections.observableArrayList());
                tripList.setVisible(false);
                tripList.setManaged(false);
                refreshList();
                return;
            }

            List<Trip> filteredTrips = trips.stream()
                    .filter(t ->
                            String.valueOf(t.getId()).contains(keyword) ||
                                    safe(t.getClientName()).toLowerCase().contains(keyword) ||
                                    safe(t.getDestination()).toLowerCase().contains(keyword) ||
                                    safe(t.getDate()).toLowerCase().contains(keyword)
                    )
                    .collect(Collectors.toList());

            tripList.setItems(FXCollections.observableArrayList(filteredTrips));
            boolean show = !filteredTrips.isEmpty();
            tripList.setVisible(show);
            tripList.setManaged(show);
        });

        tripList.setOnMouseClicked(e -> {
            Trip t = tripList.getSelectionModel().getSelectedItem();
            if (t == null) return;

            selectedTrip = t;
            selectingTrip[0] = true;

            tripSearch.setText(t.getId() + " - " + safe(t.getClientName()) + " - " + safe(t.getDestination()));

            tripList.setVisible(false);
            tripList.setManaged(false);

            selectingTrip[0] = false;

            refreshList();
        });

        // ✅ NEW DROPDOWNS (UI intact, just added)
        category = new ComboBox<>();
        category.getItems().addAll("VISA", "TICKET", "PASSPORT", "INVOICE", "OTHER");
        category.setPromptText("Category");
        category.getStyleClass().add("client-input");

        subType = new ComboBox<>();
        subType.setPromptText("Sub Type");
        subType.getStyleClass().add("client-input");

        category.setOnAction(e -> {
            subType.getItems().clear();

            switch (category.getValue()) {
                case "TICKET" -> subType.getItems().addAll("FLIGHT", "HOTEL", "ITINERARY");
                case "VISA" -> subType.getItems().addAll("TOURIST", "BUSINESS");
                case "PASSPORT" -> subType.getItems().addAll("FRONT", "BACK");
                case "INVOICE" -> subType.getItems().addAll("PAYMENT", "REFUND");
                default -> subType.getItems().add("GENERAL");
            }
        });

        Button uploadBtn = new Button("Upload Document", loadIcon("upload.png", 16));
        uploadBtn.getStyleClass().add("green-btn");
        uploadBtn.setGraphicTextGap(8);
        uploadBtn.setOnAction(e -> uploadFile());

        VBox searchBox = new VBox(8, tripSearch, tripList);
        searchBox.setPrefWidth(240);

        // ✅ ONLY change: added category + subType
        HBox top = new HBox(15, searchBox, category, subType, uploadBtn);
        top.setAlignment(Pos.CENTER_LEFT);

        listBox = new VBox(12);

        ScrollPane scrollPane = new ScrollPane(listBox);
        scrollPane.getStyleClass().add("panel");
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(430);

        root.getChildren().addAll(title, top, scrollPane);

        refreshList();
    }

    private static void uploadFile() {

        if (selectedTrip == null) {
            alert("Please select a trip first");
            return;
        }

        // ✅ NEW validation
        if (category.getValue() == null) {
            popup("Select category");
            return;
        }



        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Document");

        File selectedFile = chooser.showOpenDialog(null);
        if (selectedFile == null) return;

        try {
            File storageDir = getStorageDir();

            String extension = selectedFile.getName()
                    .substring(selectedFile.getName().lastIndexOf("."));

            String safeClient = safe(selectedTrip.getClientName())
                    .replaceAll("[^a-zA-Z0-9]", "_");

            String subTypeValue = subType.getValue() == null ? "GENERAL" : subType.getValue();;

            String newFileName =
                    "trip_" + selectedTrip.getId() + "_"
                            + safeClient + "_"
                            + category.getValue() + "_"
                            + subTypeValue + "_"
                            + UUID.randomUUID().toString().substring(0, 5)
                            + extension;

            File destFile = new File(storageDir, newFileName);

            java.nio.file.Files.copy(
                    selectedFile.toPath(),
                    destFile.toPath(),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING
            );

            // ✅ UPDATED DB save
            Document doc = new Document(
                    0,
                    selectedTrip.getUuid(),
                    selectedTrip.getClientUuid(),
                    destFile.getAbsolutePath(),
                    category.getValue(),
                    subType.getValue()
            );

            DocumentRepository.addDocument(doc);

            AppCache.reloadDocuments();
            refreshList();

            popup("Document uploaded successfully");

        } catch (Exception ex) {
            AppLogger.logError(ex, "Failed while Uploading Document");
            alert("Failed to save document");
        }
    }

    private static void refreshList() {
        listBox.getChildren().clear();

        if (selectedTrip == null) {
            Label msg = new Label("Please select a trip to view documents");
            msg.getStyleClass().add("card-subtitle");
            listBox.getChildren().add(msg);
            return;
        }

        List<Document> docs = AppCache.getDocuments().stream()
                .filter(d -> selectedTrip.getUuid().equals(d.getTripUuid()))
                .collect(Collectors.toList());

        if (docs.isEmpty()) {
            Label empty = new Label("No documents found");
            empty.getStyleClass().add("card-subtitle");
            listBox.getChildren().add(empty);
            return;
        }

        for (Document doc : docs) {
            addRow(selectedTrip, doc);
        }
    }

    private static void addRow(Trip trip, Document doc) {

        File file = new File(doc.getFilePath());

        Label name = new Label(file.getName());
        name.getStyleClass().add("card-title");

        // ✅ show type + subtype
        Label meta = new Label(
                doc.getType() + " | " + doc.getSubType() + " | " +
                        readableSize(file.length())
        );
        meta.getStyleClass().add("card-subtitle");

        VBox infoBox = new VBox(4, name, meta);

        Button view = new Button("View");
        view.getStyleClass().add("blue-btn");

        Button delete = new Button("Delete");
        delete.getStyleClass().add("delete-btn");

        view.setOnAction(e -> {
            try {
                Desktop.getDesktop().open(file);
            } catch (Exception ex) {
                alert("Cannot open file");
            }
        });

        delete.setOnAction(e -> {
            DocumentRepository.deleteDocument(doc.getUuid());
            AppCache.reloadDocuments();
            refreshList();
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox row = new HBox(12, infoBox, spacer, view, delete);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("card");

        listBox.getChildren().add(row);
    }

    private static File getStorageDir() {
        File dir = new File(System.getProperty("user.home") + "/KP_Tours_Documents");
        if (!dir.exists()) dir.mkdirs();
        return dir;
    }

    private static String readableSize(long size) {
        if (size < 1024) return size + " B";
        if (size < 1024 * 1024) return (size / 1024) + " KB";
        return (size / (1024 * 1024)) + " MB";
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static void alert(String msg) {
        new Alert(Alert.AlertType.WARNING, msg).show();
    }
}