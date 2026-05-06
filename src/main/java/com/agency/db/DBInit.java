package com.agency.db;

import java.sql.Connection;
import java.sql.Statement;

public class DBInit {

    public static void initialize() {

        String clientsTable =
                "CREATE TABLE IF NOT EXISTS clients (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "uuid TEXT UNIQUE NOT NULL," +
                        "name TEXT NOT NULL," +
                        "phone TEXT NOT NULL," +
                        "email TEXT," +
                        "city TEXT" +
                        ");";

        String tripsTable =
                "CREATE TABLE IF NOT EXISTS trips (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "uuid TEXT UNIQUE NOT NULL," +
                        "client_uuid TEXT NOT NULL," +
                        "client_id INTEGER," + // UI only
                        "client_name TEXT," +
                        "destination TEXT," +
                        "date TEXT," +
                        "type TEXT," +
                        "status TEXT," +
                        "airline_name TEXT," +
                        "purchase_value REAL," +
                        "sell_value REAL," +
                        "service_fee REAL" +
                        ");";

        String documentsTable =
                "CREATE TABLE IF NOT EXISTS documents (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "uuid TEXT UNIQUE NOT NULL," +
                        "trip_uuid TEXT," +
                        "client_uuid TEXT," +
                        "file_path TEXT," +
                        "type TEXT," +
                        "sub_type TEXT" +
                        ");";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(clientsTable);
            stmt.execute(tripsTable);
            stmt.execute(documentsTable);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}