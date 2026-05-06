package com.agency.db;

import com.agency.model.Client;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClientRepository {

    public static void addClient(Client c) {
        try (Connection conn = DBConnection.getConnection()) {
            addClient(conn, c);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void addClient(Connection conn, Client c) throws SQLException {
        String sql = "INSERT INTO clients (uuid, name, phone, email, city) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getUuid());
            ps.setString(2, c.getName());
            ps.setString(3, c.getPhone());
            ps.setString(4, c.getEmail());
            ps.setString(5, c.getCity());
            ps.executeUpdate();
        }
    }



    public static List<Client> getAllClients() {
        List<Client> list = new ArrayList<>();
        String sql = "SELECT * FROM clients ORDER BY id DESC";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Client c = new Client(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("phone"),
                        rs.getString("email"),
                        rs.getString("city")
                );

                c.setUuid(rs.getString("uuid"));
                list.add(c);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public static Client getClientById(int id) {
        String sql = "SELECT * FROM clients WHERE id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Client c = new Client(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("phone"),
                            rs.getString("email"),
                            rs.getString("city")
                    );

                    c.setUuid(rs.getString("uuid"));
                    return c;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static Client getClientByUuid(Connection conn, String uuid) throws SQLException {
        String sql = "SELECT * FROM clients WHERE uuid=?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, uuid);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Client c = new Client(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("phone"),
                            rs.getString("email"),
                            rs.getString("city")
                    );
                    c.setUuid(rs.getString("uuid"));
                    return c;
                }
            }
        }
        return null;
    }

    public static boolean existsByUuid(Connection conn, String uuid) throws SQLException {
        String sql = "SELECT 1 FROM clients WHERE uuid=?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, uuid);
            return ps.executeQuery().next();
        }
    }

    public static boolean hasTrips(String clientUuid) {
        String sql = "SELECT COUNT(*) FROM trips WHERE client_uuid=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, clientUuid);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }

    public static void updateClient(Client c) {
        String sql = "UPDATE clients SET name=?, phone=?, email=?, city=? WHERE uuid=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, c.getName());
            ps.setString(2, c.getPhone());
            ps.setString(3, c.getEmail());
            ps.setString(4, c.getCity());
            ps.setString(5, c.getUuid());

            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void deleteClient(String uuid) {
        if (hasTrips(uuid)) {
            System.out.println("Client has trips. Cannot delete.");
            return;
        }

        String sql = "DELETE FROM clients WHERE uuid=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, uuid);
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}