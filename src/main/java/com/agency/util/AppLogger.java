package com.agency.util;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static com.fasterxml.jackson.databind.util.ClassUtil.getRootCause;

public class AppLogger {

    private static final String LOG_DIR =
            System.getProperty("user.home") + "/LogFiles";

    private static final String LOG_FILE =
            LOG_DIR + "/error.log";

    public static void logError(Exception e, String context) {

        try {
            File dir = new File(LOG_DIR);
            if (!dir.exists()) dir.mkdirs();

            PrintWriter out = new PrintWriter(
                    new BufferedWriter(new FileWriter(LOG_FILE, true))
            );

            String time = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));

            StackTraceElement origin = findOrigin(e);

            String originClass = origin != null ? origin.getClassName() : "UnknownClass";
            String originMethod = origin != null ? origin.getMethodName() : "UnknownMethod";
            int line = origin != null ? origin.getLineNumber() : -1;


          out.println("-----------------------------------------------");

            // 🔥 BASIC INFO
            out.println(" CLASS     : " + originClass);
            out.println(" METHOD    : " + originMethod);
            out.println(" LINE      : " + line);


            out.println(" TIME      : " + time);
            out.println(" CONTEXT   : " + context);
            out.println(" MESSAGE    : " + e.getMessage());
            out.println(" TYPE      : " + e.getClass().getSimpleName());



            // 🔥 ROOT CAUSE (VERY IMPORTANT)
            Throwable root = getRootCause(e);
            out.println(" ROOT CAUSE: " + root.getMessage());

            out.println("\n CAUSE CHAIN:");
            Throwable t = e;
            int level = 1;

            while (t != null && level <= 5) {
                out.println("   [" + level + "] " + t.getClass().getSimpleName()
                        + " → " + t.getMessage());
                t = t.getCause();
                level++;
            }
            out.println("\n");

            out.close();
        } catch (Exception ex) {
            ex.printStackTrace(); // last fallback
        }
    }

    private static StackTraceElement findOrigin(Exception e) {

        for (StackTraceElement element : e.getStackTrace()) {

            String className = element.getClassName();

            // ignore Java internal + Jackson + JDBC
            if (!className.startsWith("java.")
                    && !className.startsWith("com.fasterxml.jackson")
                    && !className.startsWith("org.sqlite")) {
                return element;
            }
        }
        return null;
    }
}