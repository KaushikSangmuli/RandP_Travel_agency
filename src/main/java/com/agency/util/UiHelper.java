package com.agency.util;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class UiHelper {

    public static void showLoadingThen(VBox root, Runnable action) {

        ProgressIndicator loader = new ProgressIndicator();
        loader.setPrefSize(35, 35);

        VBox loadingBox = new VBox(12, loader, new Label("Loading..."));
        loadingBox.setAlignment(Pos.CENTER);
        loadingBox.setPrefHeight(400);

        root.getChildren().setAll(loadingBox);

        PauseTransition pause = new PauseTransition(Duration.millis(90));
        pause.setOnFinished(e -> action.run());
        pause.play();
    }
}