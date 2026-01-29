package seatopia.repo;

import seatopia.db.Database;
import seatopia.model.Restaurant;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class RestaurantRepo {

    public Restaurant save(Restaurant restaurant) throws SQLException {
        String sql = """
            INSERT INTO restaurants (user_id, name, address, cuisine_type, opening_hours)
            VALUES (?, ?, ?, ?, ?)
        """;

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, restaurant.getUserId());
            stmt.setString(2, restaurant.getName());
            stmt.setString(3, restaurant.getAddress());
            stmt.setString(4, restaurant.getCuisineType());
            stmt.setString(5, restaurant.getOpeningHours());

            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    return findById(keys.getInt(1));
                }
            }
        }
        return null;
    }

    public Restaurant findByUserId(int userId) throws SQLException {
        String sql = "SELECT * FROM restaurants WHERE user_id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) return null;
                return map(rs);
            }
        }
    }

    public Restaurant findById(int id) throws SQLException {
        String sql = "SELECT * FROM restaurants WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) return null;
                return map(rs);
            }
        }
    }

    public List<Restaurant> findAll() throws SQLException {
        String sql = "SELECT * FROM restaurants ORDER BY name";
        List<Restaurant> result = new ArrayList<>();

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                result.add(map(rs));
            }
        }

        return result;
    }

    private Restaurant map(ResultSet rs) throws SQLException {
        return new Restaurant(
                rs.getInt("id"),
                rs.getInt("user_id"),
                rs.getString("name"),
                rs.getString("address"),
                rs.getString("cuisine_type"),
                rs.getString("opening_hours")
        );
    }
}
