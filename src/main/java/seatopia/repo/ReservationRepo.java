package seatopia.repo;

import seatopia.db.Database;
import seatopia.model.Reservation;
import seatopia.model.ReservationStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReservationRepo {

    public Reservation save(Reservation reservation) throws SQLException {
        String sql = """
            INSERT INTO reservations
            (restaurant_id, table_id, client_id, datetime, people_count, status)
            VALUES (?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, reservation.getRestaurantId());
            stmt.setInt(2, reservation.getTableId());
            stmt.setInt(3, reservation.getClientId());
            stmt.setString(4, reservation.getDateTime().toString());
            stmt.setInt(5, reservation.getPeopleCount());
            stmt.setString(6, reservation.getStatus().name());

            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    return findById(keys.getInt(1));
                }
            }
        }
        return null;
    }

    public Reservation findById(int id) throws SQLException {
        String sql = "SELECT * FROM reservations WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) return null;
                return map(rs);
            }
        }
    }

    public List<Reservation> findByClientId(int clientId) throws SQLException {
        String sql = """
            SELECT * FROM reservations
            WHERE client_id = ?
            ORDER BY datetime DESC
        """;

        List<Reservation> result = new ArrayList<>();

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, clientId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) result.add(map(rs));
            }
        }

        return result;
    }

    public List<Reservation> findByRestaurantAndDay(int restaurantId, LocalDate day) throws SQLException {
        String start = day.atStartOfDay().toString();
        String end = day.plusDays(1).atStartOfDay().toString();

        String sql = """
            SELECT * FROM reservations
            WHERE restaurant_id = ?
              AND datetime >= ?
              AND datetime < ?
            ORDER BY datetime
        """;

        List<Reservation> result = new ArrayList<>();

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, restaurantId);
            stmt.setString(2, start);
            stmt.setString(3, end);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) result.add(map(rs));
            }
        }

        return result;
    }

    public boolean existsConflict(int tableId, LocalDateTime dateTime) throws SQLException {
        String sql = """
            SELECT 1 FROM reservations
            WHERE table_id = ?
              AND datetime = ?
              AND status IN ('PENDING', 'CONFIRMED')
            LIMIT 1
        """;

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, tableId);
            stmt.setString(2, dateTime.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void updateStatus(int reservationId, ReservationStatus status) throws SQLException {
        String sql = "UPDATE reservations SET status = ? WHERE id = ?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status.name());
            stmt.setInt(2, reservationId);
            stmt.executeUpdate();
        }
    }

    /**
     * Schimbă statusul DOAR dacă statusul curent este expectedCurrent.
     * Returnează true dacă update-ul s-a făcut, altfel false.
     */
    public boolean updateStatusIfCurrent(int reservationId, ReservationStatus newStatus, ReservationStatus expectedCurrent)
            throws SQLException {

        String sql = "UPDATE reservations SET status=? WHERE id=? AND status=?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newStatus.name());
            stmt.setInt(2, reservationId);
            stmt.setString(3, expectedCurrent.name());

            int updated = stmt.executeUpdate();
            return updated == 1;
        }
    }

    private Reservation map(ResultSet rs) throws SQLException {
        return new Reservation(
                rs.getInt("id"),
                rs.getInt("restaurant_id"),
                rs.getInt("table_id"),
                rs.getInt("client_id"),
                LocalDateTime.parse(rs.getString("datetime")),
                rs.getInt("people_count"),
                ReservationStatus.valueOf(rs.getString("status"))
        );
    }
}
