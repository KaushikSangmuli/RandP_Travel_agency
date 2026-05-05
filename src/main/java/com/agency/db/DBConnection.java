package com.agency.db;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {


    private static final String DB_DIR =
            System.getProperty("user.home") + "/KP_Tours_Data";
    private static final String DB_URL = "jdbc:sqlite:" + DB_DIR + "/agency.db";

//    private static Connection connection;

    public static Connection getConnection() {

        try {
            // ensure folder exists (safe every time)
            new File(DB_DIR).mkdirs();
            return DriverManager.getConnection(DB_URL); // 🔥 NEW connection every time
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }

    }
}