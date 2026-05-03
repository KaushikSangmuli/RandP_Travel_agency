package com.agency.db;

import com.agency.model.Trip;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TripRepository {

    // CREATE
    public static void addTrip(Trip t) {
        String sql = "INSERT INTO trips " +
                "(client_id, client_name, destination, date, type, status, airline_name, purchase_value, sell_value, document_path, service_fee) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, t.getClientId());
            ps.setString(2, t.getClientName());
            ps.setString(3, t.getDestination());
            ps.setString(4, t.getDate());
            ps.setString(5, t.getType());
            ps.setString(6, t.getStatus());
            ps.setString(7, t.getAirlineName());
            ps.setDouble(8, t.getPurchaseValue());
            ps.setDouble(9, t.getSellValue());
            ps.setDouble(10, t.getServiceFee());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    t.setId(rs.getInt(1));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // READ ALL
    public static List<Trip> getAllTrips() {
        List<Trip> list = new ArrayList<>();

        String sql = "SELECT * FROM trips ORDER BY id DESC";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
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
                        rs.getString("airline_name"),
                        rs.getDouble("service_fee")
                );

                t.setId(rs.getInt("id"));
                list.add(t);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // READ BY ID
    public static Trip getTripById(int id) {
        String sql = "SELECT * FROM trips WHERE id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Trip t = new Trip(
                            rs.getInt("client_id"),
                            rs.getString("client_name"),
                            rs.getString("destination"),
                            rs.getString("date"),
                            rs.getString("type"),
                            rs.getString("status"),
                            rs.getDouble("purchase_value"),
                            rs.getDouble("sell_value"),
                            rs.getString("airline_name"),
                            rs.getDouble("service_fee")
                    );

                    t.setId(rs.getInt("id"));
                    return t;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    // READ BY CLIENT
    public static List<Trip> getTripsByClientId(int clientId) {
        List<Trip> list = new ArrayList<>();

        String sql = "SELECT * FROM trips WHERE client_id=? ORDER BY id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, clientId);

            try (ResultSet rs = ps.executeQuery()) {
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
                            rs.getString("airline_name"),
                            rs.getDouble("service_fee")
                    );

                    t.setId(rs.getInt("id"));
                    list.add(t);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // CHECK IF CLIENT HAS TRIPS
    public static boolean hasTripsForClient(int clientId) {
        String sql = "SELECT COUNT(*) FROM trips WHERE client_id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, clientId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // UPDATE
    public static void updateTrip(int id, Trip t) {
        String sql = "UPDATE trips SET client_id=?, client_name=?, destination=?, date=?, type=?, status=?, " +
                "airline_name=?, purchase_value=?, sell_value=?, service_fee=? WHERE id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, t.getClientId());
            ps.setString(2, t.getClientName());
            ps.setString(3, t.getDestination());
            ps.setString(4, t.getDate());
            ps.setString(5, t.getType());
            ps.setString(6, t.getStatus());
            ps.setString(7, t.getAirlineName());
            ps.setDouble(8, t.getPurchaseValue());
            ps.setDouble(9, t.getSellValue());
            ps.setDouble(10, t.getServiceFee());
            ps.setInt(11, id);

            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // DELETE
    public static void deleteTrip(int id) {
        String sql = "DELETE FROM trips WHERE id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}