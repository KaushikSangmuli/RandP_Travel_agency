package com.agency.ui;

import com.agency.db.ClientRepository;
import com.agency.db.TripRepository;
import com.agency.model.Client;
import com.agency.model.Trip;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.io.*;
import java.util.List;

public class SettingsScreen {

    private static boolean dailyBackupEnabled = false;
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


        CheckBox notifications = toggle(notificationsEnabled);


        HBox notificationRow = toggleRow("Enable Notifications", notifications);


        notifications.setOnAction(e -> {
            notificationsEnabled = notifications.isSelected();
            alert(notificationsEnabled ? "Notifications enabled" : "Notifications disabled");
        });

        app.getChildren().addAll(notificationRow);

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
            BufferedWriter writer = new BufferedWriter(new FileWriter("kp_backup.txt"));

            List<Client> clients = ClientRepository.getAllClients();
            List<Trip> trips = TripRepository.getAllTrips();

            for (Client c : clients) {
                writer.write("CLIENT|" +
                        safe(c.getName()) + "|" +
                        safe(c.getPhone()) + "|" +
                        safe(c.getEmail()) + "|" +
                        safe(c.getCity()));
                writer.newLine();
            }

            for (Trip t : trips) {
                writer.write("TRIP|" +
                        t.getClientId() + "|" +
                        safe(t.getClientName()) + "|" +
                        safe(t.getDestination()) + "|" +
                        safe(t.getDate()) + "|" +
                        safe(t.getType()) + "|" +
                        safe(t.getStatus()) + "|" +
                        t.getPurchaseValue() + "|" +
                        t.getSellValue() + "|" +
                        safe(t.getAirlineName()) + "|" +
                        t.getServiceFee());
                writer.newLine();
            }

            writer.close();
            alert("Backup saved successfully");

        } catch (Exception e) {
            e.printStackTrace();
            alert("Backup failed");
        }
    }

    private static void restoreBackup() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader("kp_backup.txt"));

            for (Trip t : TripRepository.getAllTrips()) {
                TripRepository.deleteTrip(t.getId());
            }

            for (Client c : ClientRepository.getAllClients()) {
                ClientRepository.deleteClient(c.getId());
            }

            String line;

            while ((line = reader.readLine()) != null) {
                String[] p = line.split("\\|", -1);

                if (p.length == 0) continue;

                if ("CLIENT".equals(p[0]) && p.length >= 5) {
                    ClientRepository.addClient(
                            new Client(
                                    0,
                                    p[1],
                                    p[2],
                                    p[3],
                                    p[4]
                            )
                    );
                }

                if ("TRIP".equals(p[0]) && p.length >= 11) {
                    int clientId = Integer.parseInt(p[1]);
                    String clientName = p[2];
                    String destination = p[3];
                    String date = p[4];
                    String type = p[5];
                    String status = p[6];
                    double purchaseValue = parseDouble(p[7]);
                    double sellValue = parseDouble(p[8]);
                    String airlineName = p[9];
                    double serviceFee = parseDouble(p[10]);

                    TripRepository.addTrip(
                            new Trip(
                                    clientId,
                                    clientName,
                                    destination,
                                    date,
                                    type,
                                    status,
                                    purchaseValue,
                                    sellValue,
                                    airlineName,
                                    serviceFee
                            )
                    );
                }
            }

            reader.close();
            alert("Restore completed successfully");

        } catch (Exception e) {
            e.printStackTrace();
            alert("No backup file found or restore failed");
        }
    }

    private static double parseDouble(String value) {
        try {
            return value == null || value.isEmpty() ? 0.0 : Double.parseDouble(value);
        } catch (Exception e) {
            return 0.0;
        }
    }

    private static String safe(String value) {
        return value == null ? "" : value.replace("|", " ");
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