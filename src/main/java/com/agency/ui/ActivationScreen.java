package com.agency.ui;

import com.agency.license.LicenseManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class ActivationScreen {

    public static void show(Stage stage) {

        Label title = new Label("Activate Application");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold;");

        Label subtitle = new Label("Your trial has expired. Please enter your license key to continue.");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");

        Label machineIdLabel = new Label("Machine ID:");
        machineIdLabel.setStyle("-fx-font-weight: bold;");

        TextArea machineIdArea = new TextArea(LicenseManager.getMachineId());
        machineIdArea.setEditable(false);
        machineIdArea.setWrapText(true);
        machineIdArea.setPrefRowCount(3);

        Button copyBtn = new Button("Copy Machine ID");
        copyBtn.setOnAction(e -> {
            machineIdArea.selectAll();
            machineIdArea.copy();
        });

        PasswordField licenseField = new PasswordField();
        licenseField.setPromptText("Enter License Key");
        licenseField.setPrefWidth(420);

        Label message = new Label();
        message.setStyle("-fx-text-fill: red;");

        Button activateBtn = new Button("Activate");
        activateBtn.setStyle(
                "-fx-background-color: #0f766e;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 10 24;"
        );

        activateBtn.setOnAction(e -> {
            String licenseKey = licenseField.getText().trim();

            if (licenseKey.isEmpty()) {
                message.setText("Please enter license key.");
                return;
            }

            boolean activated = LicenseManager.activateLicense(licenseKey);

            if (activated) {
                message.setStyle("-fx-text-fill: green;");
                message.setText("Activation successful.");

                LoginScreen.show(stage);
            } else {
                message.setStyle("-fx-text-fill: red;");
                message.setText("Invalid license key or this key does not belong to this system.");
            }
        });

        VBox card = new VBox(14);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(30));
        card.setMaxWidth(520);
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 16;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.18), 18, 0, 0, 6);"
        );

        card.getChildren().addAll(
                title,
                subtitle,
                machineIdLabel,
                machineIdArea,
                copyBtn,
                licenseField,
                activateBtn,
                message
        );

        StackPane root = new StackPane(card);
        root.setPadding(new Insets(40));
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #ecfeff, #f8fafc);");

        Scene scene = new Scene(root, 900, 600);
        stage.setScene(scene);
        stage.setTitle("Application Activation");
        stage.setResizable(false);
        stage.show();
    }
}