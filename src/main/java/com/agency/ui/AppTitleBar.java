package com.agency.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

public class AppTitleBar extends HBox {

    private double xOffset = 0;
    private double yOffset = 0;

    public AppTitleBar(Stage stage) {

        getStyleClass().add("custom-titlebar");

        setAlignment(Pos.CENTER);
        setPrefHeight(42);

        // Left Spacer
        Region leftSpacer = new Region();
        HBox.setHgrow(leftSpacer, Priority.ALWAYS);

        // Center Title
        Label title = new Label("K.P TOURS & TRAVELS");
        title.getStyleClass().add("titlebar-text");

        // Right Controls
        HBox controls = new HBox(8);
        controls.setAlignment(Pos.CENTER_RIGHT);

        Button minimize = new Button("—");
        Button close = new Button("✕");

        minimize.getStyleClass().add("window-btn");
        close.getStyleClass().add("close-btn");

        minimize.setOnAction(e -> stage.setIconified(true));
        close.setOnAction(e -> stage.close());

        controls.getChildren().addAll(minimize, close);

        Region rightSpacer = new Region();
        HBox.setHgrow(rightSpacer, Priority.ALWAYS);

        getChildren().addAll(leftSpacer, title, rightSpacer, controls);

        // Window Dragging
        setOnMousePressed(event -> {
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        setOnMouseDragged(event -> {
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });
    }
}