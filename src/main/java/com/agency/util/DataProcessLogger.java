package com.agency.util;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DataProcessLogger {

    private static final String BASE_DIR =
            System.getProperty("user.home") + "/LogFiles";

    private static final String BACKUP_LOG = BASE_DIR + "/backup_log.txt";
    private static final String RESTORE_LOG = BASE_DIR + "/restore_log.txt";

    private static final DateTimeFormatter FORMAT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    // ================= BACKUP LOG =================
    public static void logBackup(int clientCount, int tripCount, String filePath) {
        try {
            ensureDir();

            PrintWriter out = new PrintWriter(
                    new BufferedWriter(new FileWriter(BACKUP_LOG, true))
            );

            out.println("======================================");
            out.println(" TIME        : " + now());
            out.println(" OPERATION   : BACKUP");
            out.println(" CLIENTS     : " + clientCount);
            out.println(" TRIPS       : " + tripCount);
            out.println(" FILE        : " + filePath);
            out.println("======================================\n");

            out.close();

        } catch (Exception e) {
            AppLogger.logError(e, "Failed at Logging Backup Entries..");
        }
    }

    // ================= RESTORE LOG =================
    public static void logRestore(int clientCount, int tripCount, String filePath, boolean success) {
        try {
            ensureDir();

            PrintWriter out = new PrintWriter(
                    new BufferedWriter(new FileWriter(RESTORE_LOG, true))
            );

            out.println("======================================");
            out.println("TIME        : " + now());
            out.println("OPERATION   : RESTORE");
            out.println("CLIENTS     : " + clientCount);
            out.println("TRIPS       : " + tripCount);
            out.println("FILE        : " + filePath);
            out.println("STATUS      : " + (success ? "SUCCESS" : "FAILED"));
            out.println("======================================\n");

            out.close();

        } catch (Exception e) {
            AppLogger.logError(e, "Failed at Logging Restore Entries..");
        }
    }

    // ================= COMMON =================
    private static void ensureDir() {
        File dir = new File(BASE_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    private static String now() {
        return LocalDateTime.now().format(FORMAT);
    }
}