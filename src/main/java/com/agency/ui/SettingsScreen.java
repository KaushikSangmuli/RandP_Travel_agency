package com.agency.ui;

import com.agency.data.ClientData;
import com.agency.data.TripData;
import com.agency.model.Client;
import com.agency.model.Trip;
import javafx.scene.control.*;
import javafx.scene.layout.*;



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

            DashboardScreen.updateUserName(enteredName);
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
            DashboardScreen.setDarkMode(darkModeEnabled);
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
                new Label("Clients: " + ClientData.clients.size()),
                new Label("Trips: " + TripData.trips.size())
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
            BufferedWriter writer = new BufferedWriter(new FileWriter("kp_backup.txt"));

            for (Client c : ClientData.clients) {
                writer.write("CLIENT|" + c.getName() + "|" + c.getPhone() + "|" + c.getEmail() + "|" + c.getCity());
                writer.newLine();
            }

            for (Trip t : TripData.trips) {
                writer.write("TRIP|" + t.getClientId() + "|" + t.getClientName() + "|" +
                        t.getDestination() + "|" + t.getDate() + "|" + t.getType() + "|" +
                        t.getStatus() + "|" + t.getPurchaseValue() + "|" + t.getSellValue() + "|" +
                        t.getAirlineName());
                writer.newLine();
            }

            writer.close();
            alert("Backup saved successfully");
        } catch (Exception e) {
            alert("Backup failed");
        }
    }

    private static void restoreBackup() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader("kp_backup.txt"));

            ClientData.clients.clear();
            TripData.trips.clear();

            String line;

            while ((line = reader.readLine()) != null) {
                String[] p = line.split("\\|");

                if (p[0].equals("CLIENT")) {
                    ClientData.clients.add(new Client(p[1], p[2], p[3], p[4]));
                }

                if (p[0].equals("TRIP")) {
                    TripData.trips.add(new Trip(
                            Integer.parseInt(p[1]),
                            p[2],
                            p[3],
                            p[4],
                            p[5],
                            p[6],
                            Double.parseDouble(p[7]),
                            Double.parseDouble(p[8]),
                            p[9]
                    ));
                }
            }

            reader.close();
            alert("Restore completed successfully");
        } catch (Exception e) {
            alert("No backup file found or restore failed");
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