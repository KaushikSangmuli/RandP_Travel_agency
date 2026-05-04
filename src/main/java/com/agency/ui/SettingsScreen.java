package com.agency.ui;

import com.agency.backup.BackupData;
import com.agency.backup.BackupTrip;
import com.agency.backup.ClientWithTrips;
import com.agency.db.ClientRepository;
import com.agency.db.TripRepository;
import com.agency.model.Client;
import com.agency.model.Trip;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Properties;

import static com.agency.ui.DashboardScreen.loadIcon;

public class SettingsScreen {

    private static boolean dailyBackupEnabled = false;
    private static boolean notificationsEnabled = true;

    private static final File SETTINGS_FILE =
            new File(System.getProperty("user.home"), "kp_settings.properties");

    public static VBox getView() {
        VBox root = new VBox(22);
        root.getStyleClass().add("client-root");

        Label title = new Label("Settings");
        title.getStyleClass().add("client-title");

        VBox profile = card("Profile Settings", loadIcon("profileSetting.png", 16));

        TextField name = input("Admin Name");
        TextField email = input("admin@email.com");
        TextField loginUsername = input("Login Username");

        PasswordField loginPassword = new PasswordField();
        loginPassword.setPromptText("Login Password");
        loginPassword.getStyleClass().add("client-input");

        Properties savedProfile = loadProfileData();

        name.setText(savedProfile.getProperty("admin.name", ""));
        email.setText(savedProfile.getProperty("admin.email", ""));
        loginUsername.setText(savedProfile.getProperty("login.username", "admin"));
        loginPassword.setText(savedProfile.getProperty("login.password", "admin123"));

        DashboardScreen.updateUserName(savedProfile.getProperty("admin.name", "Admin"));

        Button saveProfile = new Button("Save Profile");
        saveProfile.getStyleClass().add("view-all-btn");

        saveProfile.setOnAction(e -> {
            String enteredName = name.getText();
            String enteredEmail = email.getText();
            String enteredUsername = loginUsername.getText();
            String enteredPassword = loginPassword.getText();

            if (enteredName == null || enteredName.trim().isEmpty()) {
                alert("Name cannot be empty");
                return;
            }

            if (enteredUsername == null || enteredUsername.trim().isEmpty()) {
                alert("Login username cannot be empty");
                return;
            }

            if (enteredPassword == null || enteredPassword.trim().isEmpty()) {
                alert("Login password cannot be empty");
                return;
            }

            saveProfileData(
                    enteredName.trim(),
                    enteredEmail == null ? "" : enteredEmail.trim(),
                    enteredUsername.trim(),
                    enteredPassword.trim()
            );

            DashboardScreen.updateUserName(enteredName.trim());

            alert("Profile and login credentials updated successfully");
        });

        profile.getChildren().addAll(
                name,
                email,
                loginUsername,
                loginPassword,
                saveProfile
        );

        VBox app = card("App Settings", loadIcon("appSetting.png", 16));

        CheckBox notifications = toggle(notificationsEnabled);
        HBox notificationRow = toggleRow("Enable Notifications", notifications);

        notifications.setOnAction(e -> {
            notificationsEnabled = notifications.isSelected();
            alert(notificationsEnabled ? "Notifications enabled" : "Notifications disabled");
        });

        app.getChildren().addAll(notificationRow);

        VBox backup = card("Backup & Data", loadIcon("backupSetting.png", 16));

        Properties settings = loadProfileData();

        dailyBackupEnabled = Boolean.parseBoolean(
                settings.getProperty("daily.backup.enabled", "false")
        );

        CheckBox dailyBackup = toggle(dailyBackupEnabled);
        HBox dailyBackupRow = toggleRow("Enable Daily Backup", dailyBackup);

        Button backupBtn = new Button("Backup Now");
        backupBtn.getStyleClass().add("orange-btn");

        Button restoreBtn = new Button("Restore Backup");
        restoreBtn.getStyleClass().add("purple-btn");

        dailyBackup.setOnAction(e -> {
            dailyBackupEnabled = dailyBackup.isSelected();

            Properties props = loadProfileData();
            props.setProperty("daily.backup.enabled", String.valueOf(dailyBackupEnabled));

            try {
                FileOutputStream out = new FileOutputStream(SETTINGS_FILE);
                props.store(out, "KP Tours Settings");
                out.close();
            } catch (Exception ex) {
                ex.printStackTrace();
                alert("Failed to save backup setting");
                return;
            }

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

        VBox info = card("App Info", loadIcon("information.png", 16));

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
                clientJson.put("name", safe(c.getName()));
                clientJson.put("phone", safe(c.getPhone()));
                clientJson.put("email", safe(c.getEmail()));
                clientJson.put("city", safe(c.getCity()));

                JSONArray tripsArray = new JSONArray();

                List<Trip> trips = TripRepository.getTripsByClientId(c.getId());

                for (Trip t : trips) {
                    JSONObject tripJson = new JSONObject();

                    tripJson.put("id", t.getId());
                    tripJson.put("clientId", t.getClientId());
                    tripJson.put("clientName", safe(t.getClientName()));
                    tripJson.put("destination", safe(t.getDestination()));
                    tripJson.put("date", safe(t.getDate()));
                    tripJson.put("type", safe(t.getType()));
                    tripJson.put("status", safe(t.getStatus()));
                    tripJson.put("purchaseValue", t.getPurchaseValue());
                    tripJson.put("sellValue", t.getSellValue());
                    tripJson.put("airlineName", safe(t.getAirlineName()));
                    tripJson.put("serviceFee", t.getServiceFee());

                    tripsArray.put(tripJson);
                }

                clientJson.put("trips", tripsArray);
                clientsArray.put(clientJson);
            }

            root.put("clients", clientsArray);

            FileWriter writer = new FileWriter(backupFile);
            writer.write(root.toString(4));
            writer.close();

            alert("Backup saved successfully:\n" + backupFile.getAbsolutePath());

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

            if (data == null || data.clients == null) {
                alert("Invalid backup file");
                return;
            }

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
                    for (BackupTrip bt : c.trips) {

                        Trip trip = new Trip(
                                bt.clientId,
                                bt.clientName,
                                bt.destination,
                                bt.date,
                                bt.type,
                                bt.status,
                                bt.purchaseValue,
                                bt.sellValue,
                                bt.airlineName,
                                bt.serviceFee
                        );

                        trip.setId(bt.id);

                        if (TripRepository.existsById(bt.id)) {
                            TripRepository.updateTrip(bt.id, trip);
                        } else {
                            TripRepository.addTripWithId(trip);
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

    private static void saveProfileData(String name, String email, String username, String password) {
        try {
            Properties props = loadProfileData();

            props.setProperty("admin.name", name);
            props.setProperty("admin.email", email);
            props.setProperty("login.username", username);
            props.setProperty("login.password", password);

            FileOutputStream out = new FileOutputStream(SETTINGS_FILE);
            props.store(out, "KP Tours Settings");
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
            alert("Failed to save profile");
        }
    }

    private static Properties loadProfileData() {
        Properties props = new Properties();

        try {
            if (SETTINGS_FILE.exists()) {
                FileInputStream in = new FileInputStream(SETTINGS_FILE);
                props.load(in);
                in.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return props;
    }

    public static void runDailyBackupIfNeeded() {
        try {
            Properties props = loadProfileData();

            boolean enabled = Boolean.parseBoolean(
                    props.getProperty("daily.backup.enabled", "false")
            );

            if (!enabled) return;

            String today = LocalDate.now().toString();
            String lastBackupDate = props.getProperty("daily.backup.last.date", "");

            if (today.equals(lastBackupDate)) return;

            boolean success = createBackupSilently();

            if (success) {
                props.setProperty("daily.backup.last.date", today);

                FileOutputStream out = new FileOutputStream(SETTINGS_FILE);
                props.store(out, "KP Tours Settings");
                out.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean createBackupSilently() {
        try {
            File dir = new File(System.getProperty("user.home") + "/KP_BACKUP");

            if (!dir.exists()) {
                dir.mkdirs();
            }

            LocalDateTime now = LocalDateTime.now();

            String fileName = String.format(
                    "%02d_%02d_%d_%02d_%02d_%02d_auto_backup.json",
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
                clientJson.put("name", safe(c.getName()));
                clientJson.put("phone", safe(c.getPhone()));
                clientJson.put("email", safe(c.getEmail()));
                clientJson.put("city", safe(c.getCity()));

                JSONArray tripsArray = new JSONArray();

                List<Trip> trips = TripRepository.getTripsByClientId(c.getId());

                for (Trip t : trips) {
                    JSONObject tripJson = new JSONObject();

                    tripJson.put("id", t.getId());
                    tripJson.put("clientId", t.getClientId());
                    tripJson.put("clientName", safe(t.getClientName()));
                    tripJson.put("destination", safe(t.getDestination()));
                    tripJson.put("date", safe(t.getDate()));
                    tripJson.put("type", safe(t.getType()));
                    tripJson.put("status", safe(t.getStatus()));
                    tripJson.put("purchaseValue", t.getPurchaseValue());
                    tripJson.put("sellValue", t.getSellValue());
                    tripJson.put("airlineName", safe(t.getAirlineName()));
                    tripJson.put("serviceFee", t.getServiceFee());

                    tripsArray.put(tripJson);
                }

                clientJson.put("trips", tripsArray);
                clientsArray.put(clientJson);
            }

            root.put("clients", clientsArray);

            FileWriter writer = new FileWriter(backupFile);
            writer.write(root.toString(4));
            writer.close();

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static VBox card(String heading, Node icon) {
        VBox box = new VBox(14);
        box.getStyleClass().add("panel");

        Label title = new Label(heading);
        title.getStyleClass().add("section-title");

        HBox header = new HBox(8, icon, title);
        header.setAlignment(Pos.CENTER_LEFT);

        box.getChildren().add(header);
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
        row.setAlignment(Pos.CENTER_LEFT);
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

    public static String getLoginUsername() {
        Properties props = loadProfileData();
        return props.getProperty("login.username", "admin");
    }

    public static String getLoginPassword() {
        Properties props = loadProfileData();
        return props.getProperty("login.password", "admin123");
    }
}