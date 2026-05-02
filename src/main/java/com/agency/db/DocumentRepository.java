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
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, tripId);
            ps.setString(2, filePath);

            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // READ BY TRIP
    public static List<Document> getDocumentsByTrip(int tripId) {
        List<Document> list = new ArrayList<>();

        String sql = "SELECT * FROM documents WHERE trip_id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, tripId);

            ResultSet rs = ps.executeQuery();

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