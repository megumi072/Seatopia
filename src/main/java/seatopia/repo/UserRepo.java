package seatopia.repo;

import seatopia.db.Database;
import seatopia.model.User;
import seatopia.model.UserRole;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class UserRepo {

    public User findByEmail(String email) throws SQLException {
        String sql = "SELECT id, email, password_hash, role FROM users WHERE email = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }

                return new User(
                        rs.getInt("id"),
                        rs.getString("email"),
                        rs.getString("password_hash"),
                        UserRole.valueOf(rs.getString("role"))
                );
            }
        }
    }

    public User save(User user) throws SQLException {
        String sql = "INSERT INTO users (email, password_hash, role) VALUES (?, ?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getPasswordHash());
            stmt.setString(3, user.getRole().name());

            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    user.setId(keys.getInt(1));
                }
            }
        }

        return user;
    }
}
