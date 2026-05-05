package com.agency;

import com.agency.db.DBInit;
import com.agency.license.LicenseManager;
import com.agency.license.TrialManager;
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
        com.agency.ui.SettingsScreen.runDailyBackupIfNeeded();

        // 1. If valid license exists, continue app
        if (LicenseManager.isApplicationAllowed()) {
            openApp(stage);
            return;
        }

        // 2. If trial is still valid, continue app
        if (TrialManager.isTrialValid()) {
            openApp(stage);
            return;
        }

        // 3. Trial expired and no valid license
        com.agency.ui.ActivationScreen.show(stage);
    }

    private void openApp(Stage stage) {

        if (com.agency.service.MaintenanceService.isUnderMaintenance()) {
            com.agency.ui.MaintenanceScreen.show(stage);
        } else {
            com.agency.ui.LoginScreen.show(stage);
        }
    }

    public static void main(String[] args) {
        launch();
    }
}