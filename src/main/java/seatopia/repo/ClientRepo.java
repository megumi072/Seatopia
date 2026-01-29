package seatopia.repo;

import seatopia.db.Database;
import seatopia.model.Client;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ClientRepo {

    public Client save(Client client) throws SQLException {
        String sql = """
            INSERT INTO clients (user_id, name, phone, rating, no_show_count, completed_count)
            VALUES (?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, client.getUserId());
            stmt.setString(2, client.getName());
            stmt.setString(3, client.getPhone());
            stmt.setDouble(4, client.getRating());
            stmt.setInt(5, client.getNoShowCount());
            stmt.setInt(6, client.getCompletedCount());

            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    return findById(keys.getInt(1));
                }
            }
        }
        return null;
    }

    public Client findByUserId(int userId) throws SQLException {
        String sql = "SELECT * FROM clients WHERE user_id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) return null;
                return map(rs);
            }
        }
    }

    public Client findById(int id) throws SQLException {
        String sql = "SELECT * FROM clients WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) return null;
                return map(rs);
            }
        }
    }

    public void incrementCompleted(int clientId) throws SQLException {
        String sql = """
            UPDATE clients
            SET completed_count = completed_count + 1
            WHERE id = ?
        """;

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, clientId);
            stmt.executeUpdate();
        }
    }

    public void incrementNoShowAndUpdateRating(int clientId) throws SQLException {
        String sql = """
            UPDATE clients
            SET no_show_count = no_show_count + 1,
                rating = CASE
                    WHEN rating - 0.5 < 1.0 THEN 1.0
                    ELSE rating - 0.5
                END
            WHERE id = ?
        """;

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, clientId);
            stmt.executeUpdate();
        }
    }

    private Client map(ResultSet rs) throws SQLException {
        return new Client(
                rs.getInt("id"),
                rs.getInt("user_id"),
                rs.getString("name"),
                rs.getString("phone"),
                rs.getDouble("rating"),
                rs.getInt("no_show_count"),
                rs.getInt("completed_count")
        );
    }
}
