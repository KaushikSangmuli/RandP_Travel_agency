package com.agency.license;

import java.io.*;
import java.time.LocalDate;
import java.util.Properties;

public class LicenseStore {

    private static File getFolder(String name) {
        File folder = new File(System.getProperty("user.home"), name);
        if (!folder.exists()) folder.mkdirs();
        return folder;
    }

    public static File getLicenseFile() {
        return new File(getFolder(".kp_travels"), "license.dat");
    }

    public static File getBackupFile() {
        return new File(getFolder(".kp_backup"), "license.dat");
    }

    public static File getMarkerFile() {
        return new File(getFolder(".kp_backup"), "marker.dat");
    }

    public static void save(String licenseKey) throws Exception {

        String machine = MachineFingerprint.generate();
        String lastRun = LocalDate.now().toString();

        String signature = LicenseCrypto.sign(licenseKey + "|" + machine + "|" + lastRun);

        Properties props = new Properties();
        props.setProperty("licenseKey", licenseKey);
        props.setProperty("machine", machine);
        props.setProperty("lastRun", lastRun);
        props.setProperty("signature", signature);

        write(getLicenseFile(), props);
        write(getBackupFile(), props);

        Properties marker = new Properties();
        marker.setProperty("machine", machine);
        marker.setProperty("created", lastRun);

        write(getMarkerFile(), marker);
    }

    public static Properties read() {
        try {
            if (getLicenseFile().exists()) return load(getLicenseFile());
            if (getBackupFile().exists()) return load(getBackupFile());
        } catch (Exception ignored) {}
        return null;
    }

    public static boolean markerExists() {
        return getMarkerFile().exists();
    }

    private static void write(File file, Properties props) throws Exception {
        try (FileOutputStream out = new FileOutputStream(file)) {
            props.store(out, "License");
        }
    }

    private static Properties load(File file) throws Exception {
        Properties p = new Properties();
        try (FileInputStream in = new FileInputStream(file)) {
            p.load(in);
        }
        return p;
    }
}