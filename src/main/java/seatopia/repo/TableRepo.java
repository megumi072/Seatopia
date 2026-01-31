package seatopia.repo;

import seatopia.db.Database;
import seatopia.model.DiningTable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class TableRepo {

    public DiningTable save(DiningTable table) throws SQLException {
        String sql = """
            INSERT INTO tables (restaurant_id, name, capacity, active)
            VALUES (?, ?, ?, ?)
        """;

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, table.getRestaurantId());
            stmt.setString(2, table.getName());
            stmt.setInt(3, table.getCapacity());
            stmt.setInt(4, table.isActive() ? 1 : 0);

            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    return new DiningTable(id, table.getRestaurantId(), table.getName(), table.getCapacity(), table.isActive());
                }
            }
        }

        return null;
    }

    public List<DiningTable> findActiveByRestaurantId(int restaurantId) throws SQLException {
        String sql = """
            SELECT * FROM tables
            WHERE restaurant_id = ? AND active = 1
            ORDER BY capacity ASC, name ASC
        """;

        List<DiningTable> result = new ArrayList<>();

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, restaurantId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.add(map(rs));
                }
            }
        }

        return result;
    }

    private DiningTable map(ResultSet rs) throws SQLException {
        return new DiningTable(
                rs.getInt("id"),
                rs.getInt("restaurant_id"),
                rs.getString("name"),
                rs.getInt("capacity"),
                rs.getInt("active") == 1
        );
    }
    public void update(int tableId, String name, int capacity) throws SQLException {
        String sql = """
        UPDATE tables
        SET name = ?, capacity = ?
        WHERE id = ?
    """;

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            stmt.setInt(2, capacity);
            stmt.setInt(3, tableId);
            stmt.executeUpdate();
        }
    }

    public void deactivate(int tableId) throws SQLException {
        String sql = "UPDATE tables SET active = 0 WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, tableId);
            stmt.executeUpdate();
        }
    }

    public boolean existsByRestaurantAndName(int restaurantId, String name) throws SQLException {
        String sql = """
        SELECT 1 FROM tables
        WHERE restaurant_id = ?
          AND lower(name) = lower(?)
        LIMIT 1
    """;

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, restaurantId);
            stmt.setString(2, name);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean existsByRestaurantAndNameExcludingId(int restaurantId, String name, int excludeTableId) throws SQLException {
        String sql = """
        SELECT 1 FROM tables
        WHERE restaurant_id = ?
          AND lower(name) = lower(?)
          AND id <> ?
        LIMIT 1
    """;

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, restaurantId);
            stmt.setString(2, name);
            stmt.setInt(3, excludeTableId);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

}
