package com.agency.ui;

//import com.agency.data.TripData;
import com.agency.db.DocumentRepository;
import com.agency.db.TripRepository;
import com.agency.model.Trip;
//import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;

import java.awt.*;
import java.io.File;
import java.util.List;

public class DocumentScreen {

    private static VBox root;
    private static ComboBox<String> tripBox;
    private static VBox listBox;

    public static VBox getView() {

        root = new VBox(20);
        root.getStyleClass().add("client-root");

        buildUI();

        return root;
    }

    private static void buildUI() {

        Label title = new Label("Documents");
        title.getStyleClass().add("client-title");

        // Trip Selection
        tripBox = new ComboBox<>();
        tripBox.setPromptText("Select Trip");


        List<Trip> trips = TripRepository.getAllTrips();

        for (Trip t : trips) {
            tripBox.getItems().add(
                    t.getId() + " - " + t.getClientName() + " - " + t.getDestination()
            );
        }

        Button uploadBtn = new Button("Upload PDF");
        uploadBtn.getStyleClass().add("client-add-btn");

        uploadBtn.setOnAction(e -> uploadFile());

        HBox top = new HBox(15, tripBox, uploadBtn);
        top.setAlignment(Pos.CENTER_LEFT);

        listBox = new VBox(10);
        listBox.getStyleClass().add("panel");
        tripBox.setOnAction(e -> refreshList());



        root.getChildren().addAll(title, top, listBox);

        refreshList();
    }

    private static void uploadFile() {

        if (tripBox.getValue() == null) {
            alert("Please select a trip");
            return;
        }

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Document");

        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Documents", "*.pdf", "*.doc", "*.docx")
        );

        File selectedFile = chooser.showOpenDialog(null);

        if (selectedFile == null) return;

        try {
            int tripId = Integer.parseInt(tripBox.getValue().split(" - ")[0]);

            File storageDir = getStorageDir();

            String newFileName = System.currentTimeMillis() + "_" + selectedFile.getName();
            File destFile = new File(storageDir, newFileName);

            java.nio.file.Files.copy(
                    selectedFile.toPath(),
                    destFile.toPath(),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING
            );

            // ✅ SAVE TO DB
            DocumentRepository.addDocument(tripId, destFile.getAbsolutePath());

            refreshList();


        } catch (Exception ex) {
            alert("Failed to save document");
        }
    }

    private static void refreshList() {

        listBox.getChildren().clear();

        List<Trip> trips = TripRepository.getAllTrips();

        // 🔥 If no trip selected → show ALL documents
        if (tripBox.getValue() == null) {

            for (Trip t : trips) {
                var docs = DocumentRepository.getDocumentsByTrip(t.getId());

                for (var doc : docs) {
                    addRow(t, doc);
                }
            }

        }
else{

        // 🔥 If trip selected → filter
        int tripId = Integer.parseInt(tripBox.getValue().split(" - ")[0]);

        var docs = DocumentRepository.getDocumentsByTrip(tripId);

        for (var doc : docs) {
            addRow(null, doc);
        }
        }

    }
    private static void alert(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setContentText(msg);
        alert.show();
    }


    private static File getStorageDir() {
        File dir = new File(System.getProperty("user.home") + "/KP_Tours_Documents");

        if (!dir.exists()) {
            dir.mkdirs();
        }

        return dir;
    }

    private static void addRow(Trip trip, com.agency.model.Document doc) {

        File file = new File(doc.getFilePath());

        String labelText = "File: " + file.getName();

        if (trip != null) {
            labelText = trip.getClientName() + " | " + trip.getDestination() + "\n" + labelText;
        }

        Label info = new Label(labelText);

        Button view = new Button("View");
        Button delete = new Button("Delete");

        view.setOnAction(e -> {
            try {
                Desktop.getDesktop().open(file);
            } catch (Exception ex) {
                alert("Cannot open file");
            }
        });

        delete.setOnAction(e -> {
            DocumentRepository.deleteDocument(doc.getId());
            refreshList();
        });

        HBox row = new HBox(15, info, view, delete);
        row.setAlignment(Pos.CENTER_LEFT);

        listBox.getChildren().add(row);
//        refreshList();
    }


}