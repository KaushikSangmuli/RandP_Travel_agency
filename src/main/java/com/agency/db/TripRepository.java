package com.agency.db;

import com.agency.db.DBConnection;
import com.agency.model.Trip;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TripRepository {

    // CREATE
    public static void addTrip(Trip t) {
        String sql = "INSERT INTO trips (client_id, client_name, destination, date, type, status, airline_name, purchase_value, sell_value, document_path) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {

            ps.setInt(1, t.getClientId());
            ps.setString(2, t.getClientName());
            ps.setString(3, t.getDestination());
            ps.setString(4, t.getDate());
            ps.setString(5, t.getType());
            ps.setString(6, t.getStatus());
            ps.setString(7, t.getAirlineName());
            ps.setDouble(8, t.getPurchaseValue());
            ps.setDouble(9, t.getSellValue());
            ps.setString(10, t.getDocumentPath());

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                int generatedId = rs.getInt(1);
                t.setId(generatedId);   // ✅ IMPORTANT
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // READ
    public static List<Trip> getAllTrips() {
        List<Trip> list = new ArrayList<>();

        String sql = "SELECT * FROM trips";

        try (Statement stmt = DBConnection.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Trip t = new Trip(
                        rs.getInt("client_id"),
                        rs.getString("client_name"),
                        rs.getString("destination"),
                        rs.getString("date"),
                        rs.getString("type"),
                        rs.getString("status"),
                        rs.getDouble("purchase_value"),
                        rs.getDouble("sell_value"),
                        rs.getString("airline_name")
                );

                t.setId(rs.getInt("id")); // 🔥 MUST DO THIS
                t.setDocumentPath(rs.getString("document_path"));

                list.add(t);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // UPDATE
    public static void updateTrip(int id, Trip t) {
        String sql = "UPDATE trips SET client_id=?, client_name=?, destination=?, date=?, type=?, status=?, airline_name=?, purchase_value=?, sell_value=? WHERE id=?";

        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {

            ps.setInt(1, t.getClientId());
            ps.setString(2, t.getClientName());
            ps.setString(3, t.getDestination());
            ps.setString(4, t.getDate());
            ps.setString(5, t.getType());
            ps.setString(6, t.getStatus());
            ps.setString(7, t.getAirlineName());
            ps.setDouble(8, t.getPurchaseValue());
            ps.setDouble(9, t.getSellValue());
            ps.setInt(10, id);

            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // DELETE
    public static void deleteTrip(int id) {
        String sql = "DELETE FROM trips WHERE id=?";

        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<Trip> getTripsByClientId(int clientId) {

        List<Trip> list = new ArrayList<>();

        String sql = "SELECT * FROM trips WHERE client_id = ?";

        try (PreparedStatement ps = DBConnection.getConnection().prepareStatement(sql)) {

            ps.setInt(1, clientId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Trip t = new Trip(
                        rs.getInt("client_id"),
                        rs.getString("client_name"),
                        rs.getString("destination"),
                        rs.getString("date"),
                        rs.getString("type"),
                        rs.getString("status"),
                        rs.getDouble("purchase_value"),
                        rs.getDouble("sell_value"),
                        rs.getString("airline_name")
                );

                t.setId(rs.getInt("id"));
                t.setDocumentPath(rs.getString("document_path"));

                list.add(t);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }


    public static void addTripWithId(Trip t) {
        String sql = "INSERT INTO trips (id, client_id, client_name, destination, date, type, status, airline_name, purchase_value, sell_value, document_path) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setInt(1, t.getId()); // 🔥 preserve ID
            ps.setInt(2, t.getClientId());
            ps.setString(3, t.getClientName());
            ps.setString(4, t.getDestination());
            ps.setString(5, t.getDate());
            ps.setString(6, t.getType());
            ps.setString(7, t.getStatus());
            ps.setString(8, t.getAirlineName());
            ps.setDouble(9, t.getPurchaseValue());
            ps.setDouble(10, t.getSellValue());
            ps.setString(11, t.getDocumentPath());

            ps.executeUpdate();
            ps.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static boolean existsById(int id) {
        String sql = "SELECT 1 FROM trips WHERE id = ?";

        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            boolean exists = rs.next();

            rs.close();
            ps.close();

            return exists;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}