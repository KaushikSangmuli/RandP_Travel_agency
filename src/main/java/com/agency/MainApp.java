package com.agency;

import com.agency.db.DBInit;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        stage.getIcons().add(
                new Image(getClass().getResourceAsStream("/icons/appIcon.png"))
        );
        DBInit.initialize();
//        com.agency.ui.SettingsScreen.runDailyBackupIfNeeded();
//
//        if (com.agency.service.MaintenanceService.isUnderMaintenance()) {
//
//            com.agency.ui.MaintenanceScreen.show(stage);
//
//        } else {
//
//            com.agency.ui.LoginScreen.show(stage);
//        }
        com.agency.ui.DashboardScreen.show(stage);

    }    public static void main(String[] args) {
        launch();
    }
}