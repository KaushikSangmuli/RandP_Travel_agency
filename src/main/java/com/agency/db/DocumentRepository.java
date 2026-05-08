package com.agency.db;

import com.agency.model.Document;
import com.agency.util.AppLogger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DocumentRepository {

    public static void addDocument(Document d) {
        try (Connection conn = DBConnection.getConnection()) {
            addDocument(conn, d);
        } catch (Exception e) {
            AppLogger.logError(e, "Failed while Adding the Document.");
        }
    }

    public static void addDocument(Connection conn, Document d) throws SQLException {
        String sql = "INSERT INTO documents " +
                "(uuid, trip_uuid, client_uuid, file_path, type, sub_type) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, d.getUuid());
            ps.setString(2, d.getTripUuid());
            ps.setString(3, d.getClientUuid());
            ps.setString(4, d.getFilePath());
            ps.setString(5, d.getType());
            ps.setString(6, d.getSubType());

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    d.setId(rs.getInt(1));
                }
            }
        }
    }

    public static boolean existsByUuid(Connection conn, String uuid) throws SQLException {
        String sql = "SELECT 1 FROM documents WHERE uuid=?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, uuid);
            return ps.executeQuery().next();
        }
    }

    public static boolean existsByUuid(String uuid) {
        try (Connection conn = DBConnection.getConnection()) {
            return existsByUuid(conn, uuid);
        } catch (Exception e) {
            AppLogger.logError(e ,"Failed while checking if Document exists from UUID");
            return false;
        }
    }

    public static List<Document> getDocumentsByTripUuid(String tripUuid) {
        List<Document> list = new ArrayList<>();
        String sql = "SELECT * FROM documents WHERE trip_uuid=? ORDER BY id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, tripUuid);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapDocument(rs));
                }
            }

        } catch (Exception e) {
            AppLogger.logError(e , "Failed while fetching Document by Trip UUID ");
        }

        return list;
    }

    public static List<Document> getDocumentsByClientUuid(String clientUuid) {
        List<Document> list = new ArrayList<>();
        String sql = "SELECT * FROM documents WHERE client_uuid=? ORDER BY id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, clientUuid);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapDocument(rs));
                }
            }

        } catch (Exception e) {
            AppLogger.logError(e,"Failed while Fetching the Documents by Client UUID");
        }

        return list;
    }

    public static List<Document> getAllDocuments() {
        List<Document> list = new ArrayList<>();
        String sql = "SELECT * FROM documents ORDER BY id DESC";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapDocument(rs));
            }

        } catch (Exception e) {
            AppLogger.logError(e , "Failed while Getting All Documents.");
        }

        return list;
    }

    public static Document getDocumentById(int id) {
        String sql = "SELECT * FROM documents WHERE id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapDocument(rs);
                }
            }

        } catch (Exception e) {
            AppLogger.logError(e, "Failed while getting Document by ID ");
        }

        return null;
    }

    public static void deleteDocument(String uuid) {
        String sql = "DELETE FROM documents WHERE uuid=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, uuid);
            ps.executeUpdate();

        } catch (Exception e) {
           AppLogger.logError(e, "Failed while Deleting the Document.");
        }
    }

    private static Document mapDocument(ResultSet rs) throws SQLException {
        Document d = new Document(
                rs.getInt("id"),
                rs.getString("trip_uuid"),
                rs.getString("client_uuid"),
                rs.getString("file_path"),
                rs.getString("type"),
                rs.getString("sub_type")
        );

        d.setUuid(rs.getString("uuid"));
        return d;
    }
}