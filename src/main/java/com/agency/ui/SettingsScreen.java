package com.agency.ui;


import com.agency.db.ClientRepository;
import com.agency.db.TripRepository;
import com.agency.model.Client;
import com.agency.model.Trip;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import javafx.stage.FileChooser;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.agency.backup.BackupData;
import com.agency.backup.ClientWithTrips;





import java.io.*;

public class SettingsScreen {

    private static boolean dailyBackupEnabled = false;
    private static boolean darkModeEnabled = false;
    private static boolean notificationsEnabled = true;

    public static VBox getView() {
        VBox root = new VBox(22);
        root.getStyleClass().add("client-root");

        Label title = new Label("Settings");
        title.getStyleClass().add("client-title");

        VBox profile = card("👤 Profile Settings");

        TextField name = input("Admin Name");
        TextField email = input("admin@email.com");

        Button saveProfile = new Button("Save Profile");
        saveProfile.getStyleClass().add("view-all-btn");
        saveProfile.setOnAction(e -> {
            String enteredName = name.getText();

            if (enteredName == null || enteredName.isEmpty()) {
                alert("Name cannot be empty");
                return;
            }

//            DashboardScreen.updateUserName(enteredName);
            alert("Profile updated successfully");
        });

        profile.getChildren().addAll(name, email, saveProfile);

        VBox app = card("⚙ App Settings");

        CheckBox darkMode = toggle(darkModeEnabled);
        CheckBox notifications = toggle(notificationsEnabled);

        HBox darkRow = toggleRow("Enable Dark Mode", darkMode);
        HBox notificationRow = toggleRow("Enable Notifications", notifications);

        darkMode.setOnAction(e -> {
            darkModeEnabled = darkMode.isSelected();
//            DashboardScreen.setDarkMode(darkModeEnabled);
            alert(darkModeEnabled ? "Dark mode enabled" : "Dark mode disabled");
        });

        notifications.setOnAction(e -> {
            notificationsEnabled = notifications.isSelected();
            alert(notificationsEnabled ? "Notifications enabled" : "Notifications disabled");
        });

        app.getChildren().addAll(darkRow, notificationRow);

        VBox backup = card("💾 Backup & Data");

        CheckBox dailyBackup = toggle(dailyBackupEnabled);
        HBox dailyBackupRow = toggleRow("Enable Daily Backup", dailyBackup);

        Button backupBtn = new Button("Backup Now");
        backupBtn.getStyleClass().add("orange-btn");

        Button restoreBtn = new Button("Restore Backup");
        restoreBtn.getStyleClass().add("purple-btn");

        dailyBackup.setOnAction(e -> {
            dailyBackupEnabled = dailyBackup.isSelected();
            alert(dailyBackupEnabled ? "Daily backup enabled" : "Daily backup disabled");
        });

        backupBtn.setOnAction(e -> {
            if (!dailyBackupEnabled) {
                alert("Please enable Daily Backup first");
                return;
            }
            createBackup();
        });

        restoreBtn.setOnAction(e -> restoreBackup());

        backup.getChildren().addAll(dailyBackupRow, backupBtn, restoreBtn);

        VBox info = card("ℹ App Info");
        info.getChildren().addAll(
                new Label("Version: 1.0.0"),
                new Label("Company: KP Tours & Travels"),
                new Label("Clients: " + ClientRepository.getAllClients().size()),
                new Label("Trips: " + TripRepository.getAllTrips().size())
        );

        HBox top = new HBox(20, profile, app);
        HBox bottom = new HBox(20, backup, info);

        for (VBox box : new VBox[]{profile, app, backup, info}) {
            HBox.setHgrow(box, Priority.ALWAYS);
            box.setMaxWidth(Double.MAX_VALUE);
            box.setMinHeight(220);
        }

        root.getChildren().addAll(title, top, bottom);
        return root;
    }

    private static void createBackup() {
        try {
            File dir = new File(System.getProperty("user.home") + "/KP_BACKUP");

            if (!dir.exists()) {
                dir.mkdirs();
            }

            LocalDateTime now = LocalDateTime.now();

            String fileName = String.format(
                    "%02d_%02d_%d_%02d_%02d_%02d_backup.json",
                    now.getDayOfMonth(),
                    now.getMonthValue(),
                    now.getYear(),
                    now.getHour(),
                    now.getMinute(),
                    now.getSecond()
            );

            File backupFile = new File(dir, fileName);

            JSONObject root = new JSONObject();
            JSONArray clientsArray = new JSONArray();

            List<Client> clients = ClientRepository.getAllClients();

            for (Client c : clients) {

                JSONObject clientJson = new JSONObject();
                clientJson.put("id", c.getId());
                clientJson.put("name", c.getName());
                clientJson.put("phone", c.getPhone());
                clientJson.put("email", c.getEmail());
                clientJson.put("city", c.getCity());

                List<Trip> trips = TripRepository.getTripsByClientId(c.getId());
                JSONArray tripsArray = new JSONArray();

                for (Trip t : trips) {
                    JSONObject tripJson = new JSONObject();

                    tripJson.put("id", t.getId());
                    tripJson.put("clientName", t.getClientName());
                    tripJson.put("clientId", c.getId()); // 🔥 IMPORTANT
                    tripJson.put("destination", t.getDestination());
                    tripJson.put("date", t.getDate());
                    tripJson.put("type", t.getType());
                    tripJson.put("status", t.getStatus());
                    tripJson.put("airlineName", t.getAirlineName());
                    tripJson.put("purchaseValue", t.getPurchaseValue());
                    tripJson.put("sellValue", t.getSellValue());
                    tripJson.put("documentPath", t.getDocumentPath());

                    tripsArray.put(tripJson);
                }

                clientJson.put("trips", tripsArray);
                clientsArray.put(clientJson);
            }

            root.put("clients", clientsArray);

            FileWriter file = new FileWriter(backupFile);
            file.write(root.toString(4));
            file.close();

            alert("Backup saved:\n" + backupFile.getAbsolutePath());

        } catch (Exception e) {
            e.printStackTrace();
            alert("Backup failed");
        }
    }
    private static void restoreBackup() {
        try {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Select Backup File");

            chooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("JSON Files", "*.json")
            );

            File defaultDir = new File(System.getProperty("user.home") + "/KP_BACKUP");
            if (defaultDir.exists()) {
                chooser.setInitialDirectory(defaultDir);
            }

            File file = chooser.showOpenDialog(null);

            if (file == null) {
                alert("No file selected");
                return;
            }

            ObjectMapper mapper = new ObjectMapper();
            BackupData data = mapper.readValue(file, BackupData.class);

            for (ClientWithTrips c : data.clients) {

                Client client = new Client(
                        c.id,
                        c.name,
                        c.phone,
                        c.email,
                        c.city
                );

                if (ClientRepository.existsById(c.id)) {
                    ClientRepository.updateClient(client);
                } else {
                    ClientRepository.addClientWithId(client);
                }

                if (c.trips != null) {
                    for (Trip t : c.trips) {

                        if (TripRepository.existsById(t.getId())) {
                            TripRepository.updateTrip(t.getId(), t);
                        } else {
                            TripRepository.addTripWithId(t);
                        }
                    }
                }
            }

            alert("Restore completed successfully");

        } catch (Exception e) {
            e.printStackTrace();
            alert("Restore failed");
        }
    }

    private static VBox card(String heading) {
        VBox box = new VBox(14);
        box.getStyleClass().add("panel");

        Label title = new Label(heading);
        title.getStyleClass().add("section-title");

        box.getChildren().add(title);
        return box;
    }

    private static CheckBox toggle(boolean selected) {
        CheckBox checkBox = new CheckBox();
        checkBox.setSelected(selected);
        checkBox.getStyleClass().add("toggle-switch");
        return checkBox;
    }

    private static HBox toggleRow(String text, CheckBox toggle) {
        Label label = new Label(text);
        HBox row = new HBox(12, label, toggle);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        return row;
    }

    private static TextField input(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.getStyleClass().add("client-input");
        return field;
    }

    private static void alert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}