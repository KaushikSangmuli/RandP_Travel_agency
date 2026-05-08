package com.agency.db;

import com.agency.model.Trip;
import com.agency.util.AppLogger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TripRepository {

    public static void addTrip(Trip t) {
        try (Connection conn = DBConnection.getConnection()) {
            addTrip(conn, t);
        } catch (Exception e) {
            AppLogger.logError(e, "Failed while Adding the Trip.");
        }
    }

    public static void addTrip(Connection conn, Trip t) throws SQLException {
        String sql = "INSERT INTO trips " +
                "(uuid, client_uuid, client_id, client_name, destination, date, type, status, airline_name, purchase_value, sell_value, service_fee) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, t.getUuid());
            ps.setString(2, t.getClientUuid());
            ps.setInt(3, t.getClientId());
            ps.setString(4, t.getClientName());
            ps.setString(5, t.getDestination());
            ps.setString(6, t.getDate());
            ps.setString(7, t.getType());
            ps.setString(8, t.getStatus());
            ps.setString(9, t.getAirlineName());
            ps.setDouble(10, t.getPurchaseValue());
            ps.setDouble(11, t.getSellValue());
            ps.setDouble(12, t.getServiceFee());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    t.setId(rs.getInt(1));
                }
            }
        }
    }

    public static boolean existsByUuid(Connection conn, String uuid) throws SQLException {
        String sql = "SELECT 1 FROM trips WHERE uuid=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, uuid);
            return ps.executeQuery().next();
        }
    }

    public static boolean existsByUuid(String uuid) {
        try (Connection conn = DBConnection.getConnection()) {
            return existsByUuid(conn, uuid);
        } catch (Exception e) {
           AppLogger.logError(e, "Failed while Checking If Trip exists by UUID.");
            return false;
        }
    }

    public static List<Trip> getAllTrips() {
        List<Trip> list = new ArrayList<>();
        String sql = "SELECT * FROM trips ORDER BY id DESC";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapTrip(rs));
            }

        } catch (Exception e) {
            AppLogger.logError(e , "Failed while Fetching all The Trips.");
        }

        return list;
    }

    public static List<Trip> getTripsByClientUuid(String clientUuid) {
        List<Trip> list = new ArrayList<>();
        String sql = "SELECT * FROM trips WHERE client_uuid=? ORDER BY id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, clientUuid);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapTrip(rs));
                }
            }

        } catch (Exception e) {
            AppLogger.logError(e, "Failed While Getting Trips by Clients by UUID.");
        }

        return list;
    }

    public static void updateTrip(Trip t) {
        String sql = "UPDATE trips SET client_id=?, client_uuid=?, client_name=?, destination=?, date=?, type=?, status=?, " +
                "airline_name=?, purchase_value=?, sell_value=?, service_fee=? WHERE uuid=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, t.getClientId());
            ps.setString(2, t.getClientUuid());
            ps.setString(3, t.getClientName());
            ps.setString(4, t.getDestination());
            ps.setString(5, t.getDate());
            ps.setString(6, t.getType());
            ps.setString(7, t.getStatus());
            ps.setString(8, t.getAirlineName());
            ps.setDouble(9, t.getPurchaseValue());
            ps.setDouble(10, t.getSellValue());
            ps.setDouble(11, t.getServiceFee());
            ps.setString(12, t.getUuid());

            ps.executeUpdate();

        } catch (Exception e) {
            AppLogger.logError(e, "Failed While Update Trip "+t.getClientName()+t.getDestination());
        }
    }

    public static void deleteTrip(String uuid) {
        String sql = "DELETE FROM trips WHERE uuid=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, uuid);
            ps.executeUpdate();

        } catch (Exception e) {
            AppLogger.logError(e, "Failed while Deleting the Trip.");
        }
    }

    private static Trip mapTrip(ResultSet rs) throws SQLException {
        Trip t = new Trip(
                rs.getInt("client_id"),
                rs.getString("client_uuid"),
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
        t.setUuid(rs.getString("uuid"));

        return t;
    }
}