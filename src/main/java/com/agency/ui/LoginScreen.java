package com.agency.ui;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;

public class LoginScreen {

    public static void show(Stage stage) {

        // LEFT BRANDING
        Label bigTitle = new Label("K.P TOURS & TRAVELS");
        bigTitle.getStyleClass().add("hero-title");

        Label tagline = new Label(
                "Discover destinations,\nmanage journeys,\nand travel smarter."
        );
        tagline.getStyleClass().add("hero-subtitle");

        VBox leftSection = new VBox(18, bigTitle, tagline);
        leftSection.setAlignment(Pos.CENTER_LEFT);
        leftSection.setPadding(new Insets(0, 0, 0, 120));
        leftSection.getStyleClass().add("hero-section");

        // LOGIN SECTION
        Label brand = new Label("Welcome Back");
        brand.getStyleClass().add("login-brand");

        Label subtitle = new Label("Sign in to continue");
        subtitle.getStyleClass().add("login-subtitle");

        TextField username = new TextField();
        username.setPromptText("Username");

        PasswordField password = new PasswordField();
        password.setPromptText("Password");

        Hyperlink forgotPassword = new Hyperlink("Forgot Password?");
        forgotPassword.getStyleClass().add("forgot-password");

        Label message = new Label();
        message.getStyleClass().add("error-text");

        Button loginBtn = new Button("Login");

        loginBtn.setOnAction(e -> handleLogin(username, password, message));
        password.setOnAction(e -> handleLogin(username, password, message));

        VBox card = new VBox(
                18,
                brand,
                subtitle,
                username,
                password,
                loginBtn,
                forgotPassword,
                message
        );

        card.setAlignment(Pos.CENTER);
        card.getStyleClass().add("login-card");

        // MAIN CONTENT
        HBox content = new HBox(leftSection, card);
        content.setAlignment(Pos.CENTER);
        content.setSpacing(90);
        content.setTranslateX(-120);

        // BACKGROUND GIF
        // GIF Background
        // 🔥 Load GIF properly (IMPORTANT FIX)
        Image gif = new Image(
                LoginScreen.class.getResource("/animations/login.gif").toExternalForm(),
                0, 0, true, true, true   // 👈 THIS enables smooth loading + animation
        );

        ImageView bgGif = new ImageView(gif);

// 🔥 Fullscreen responsive (correct)
        bgGif.fitWidthProperty().bind(stage.widthProperty());
        bgGif.fitHeightProperty().bind(stage.heightProperty());
        bgGif.setPreserveRatio(false);

// 🔥 Slight dimming for better UI visibility
        bgGif.setOpacity(0.85);


// 🔥 Overlay (make sure it actually covers screen)
        Pane overlay = new Pane();
        overlay.getStyleClass().add("login-overlay");

// Bind overlay to parent (BETTER than stage)
        overlay.prefWidthProperty().bind(bgGif.fitWidthProperty());
        overlay.prefHeightProperty().bind(bgGif.fitHeightProperty());

        // Root Layout
        StackPane root = new StackPane(bgGif, overlay, content);
        root.setAlignment(Pos.CENTER);

        // Scene
        Scene scene = new Scene(root, 1400, 750);

        scene.getStylesheets().addAll(
                LoginScreen.class.getResource("/css/common.css").toExternalForm(),
                LoginScreen.class.getResource("/css/login.css").toExternalForm()
        );

        // Entry Animation
        FadeTransition fade = new FadeTransition(Duration.seconds(1.2), content);
        fade.setFromValue(0);
        fade.setToValue(1);

        TranslateTransition slide = new TranslateTransition(Duration.seconds(1), content);
        slide.setFromY(50);
        slide.setToY(0);

        fade.play();
        slide.play();

        stage.setScene(scene);
        stage.show();

        username.requestFocus();
    }

    private static void handleLogin(TextField username, PasswordField password, Label message) {

        if (username.getText().equals(SettingsScreen.getLoginUsername())
                && password.getText().equals(SettingsScreen.getLoginPassword())) {

            message.setText("Login Success");

            Stage stage = (Stage) username.getScene().getWindow();

            boolean wasMaximized = stage.isMaximized();
            double currentWidth = stage.getWidth();
            double currentHeight = stage.getHeight();

            DashboardScreen.show(stage);

            stage.setWidth(currentWidth);
            stage.setHeight(currentHeight);
            stage.setMaximized(wasMaximized);

        } else {
            message.setText("Invalid credentials");
        }
    }
}