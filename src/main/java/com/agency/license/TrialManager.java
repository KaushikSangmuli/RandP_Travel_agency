package com.agency.license;

import java.io.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Properties;

public class TrialManager {

    private static final int TRIAL_DAYS = 7;

    private static File getTrialFile() {
        File folder = new File(System.getProperty("user.home"), ".kp_trial");
        if (!folder.exists()) folder.mkdirs();
        return new File(folder, "trial.dat");
    }

    private static File getMarkerFile() {
        File folder = new File(System.getProperty("user.home"), ".kp_trial_marker");
        if (!folder.exists()) folder.mkdirs();
        return new File(folder, "marker.dat");
    }

    public static boolean isTrialValid() {
        try {
            File trialFile = getTrialFile();
            File markerFile = getMarkerFile();

            // 🔹 First ever run
            if (!trialFile.exists() && !markerFile.exists()) {
                createTrial(trialFile, markerFile);
                return true;
            }

            // 🔴 Tampering: trial deleted but marker exists
            if (!trialFile.exists() && markerFile.exists()) {
                return false;
            }

            // 🔴 Corrupt case
            if (!trialFile.exists()) return false;

            Properties p = new Properties();
            try (FileInputStream in = new FileInputStream(trialFile)) {
                p.load(in);
            }

            String startDateStr = p.getProperty("startDate");
            String signature = p.getProperty("signature");
            String machine = p.getProperty("machine");

            if (startDateStr == null || signature == null || machine == null) {
                return false;
            }

            // 🔹 Validate machine binding
            String currentMachine = MachineFingerprint.generate();
            if (!currentMachine.equals(machine)) {
                return false;
            }

            // 🔹 Validate signature
            String raw = startDateStr + "|" + machine;
            String expectedSig = LicenseCrypto.sign(raw);

            if (!expectedSig.equals(signature)) {
                return false;
            }

            LocalDate startDate = LocalDate.parse(startDateStr);
            LocalDate today = LocalDate.now();

            // 🔹 Date rollback protection
            if (today.isBefore(startDate)) {
                return false;
            }

            long days = ChronoUnit.DAYS.between(startDate, today);

            return days <= TRIAL_DAYS;

        } catch (Exception e) {
            return false;
        }
    }

    private static void createTrial(File trialFile, File markerFile) throws Exception {

        String today = LocalDate.now().toString();
        String machine = MachineFingerprint.generate();

        String raw = today + "|" + machine;
        String signature = LicenseCrypto.sign(raw);

        Properties p = new Properties();
        p.setProperty("startDate", today);
        p.setProperty("machine", machine);
        p.setProperty("signature", signature);

        try (FileOutputStream out = new FileOutputStream(trialFile)) {
            p.store(out, "Trial");
        }

        // Marker (simple presence check)
        Properties marker = new Properties();
        marker.setProperty("created", today);

        try (FileOutputStream out = new FileOutputStream(markerFile)) {
            marker.store(out, "Marker");
        }
    }
}