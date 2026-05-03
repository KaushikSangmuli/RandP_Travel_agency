package com.agency.db;

import com.agency.model.Document;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DocumentRepository {

    // CREATE
    public static void addDocument(int tripId, String filePath) {
        String sql = "INSERT INTO documents (trip_id, file_path) VALUES (?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, tripId);
            ps.setString(2, filePath);

            ps.executeUpdate();

            // optional: if you want generated id later
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    // you can use it if needed
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // READ BY TRIP
    public static List<Document> getDocumentsByTrip(int tripId) {
        List<Document> list = new ArrayList<>();

        String sql = "SELECT * FROM documents WHERE trip_id=? ORDER BY id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, tripId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Document(
                            rs.getInt("id"),
                            rs.getInt("trip_id"),
                            rs.getString("file_path")
                    ));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // READ ALL (useful for reports / admin)
    public static List<Document> getAllDocuments() {
        List<Document> list = new ArrayList<>();

        String sql = "SELECT * FROM documents ORDER BY id DESC";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(new Document(
                        rs.getInt("id"),
                        rs.getInt("trip_id"),
                        rs.getString("file_path")
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // READ BY ID
    public static Document getDocumentById(int id) {
        String sql = "SELECT * FROM documents WHERE id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Document(
                            rs.getInt("id"),
                            rs.getInt("trip_id"),
                            rs.getString("file_path")
                    );
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    // DELETE
    public static void deleteDocument(int id) {
        String sql = "DELETE FROM documents WHERE id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}