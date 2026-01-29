package seatopia.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class Database {
    private static final String URL = "jdbc:sqlite:seatopia.db";

    private Database() {}

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void init() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("PRAGMA foreign_keys = ON;");

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    email TEXT NOT NULL UNIQUE,
                    password_hash TEXT NOT NULL,
                    role TEXT NOT NULL
                );
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS clients (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL UNIQUE,
                    name TEXT NOT NULL,
                    phone TEXT NOT NULL,
                    rating REAL DEFAULT 5.0,
                    no_show_count INTEGER DEFAULT 0,
                    completed_count INTEGER DEFAULT 0,
                    FOREIGN KEY (user_id) REFERENCES users(id)
                );
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS restaurants (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_id INTEGER NOT NULL UNIQUE,
                    name TEXT NOT NULL,
                    address TEXT NOT NULL,
                    cuisine_type TEXT,
                    opening_hours TEXT,
                    FOREIGN KEY (user_id) REFERENCES users(id)
                );
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS tables (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    restaurant_id INTEGER NOT NULL,
                    name TEXT NOT NULL,
                    capacity INTEGER NOT NULL,
                    active INTEGER DEFAULT 1,
                    FOREIGN KEY (restaurant_id) REFERENCES restaurants(id)
                );
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS reservations (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    restaurant_id INTEGER NOT NULL,
                    table_id INTEGER NOT NULL,
                    client_id INTEGER NOT NULL,
                    datetime TEXT NOT NULL,
                    people_count INTEGER NOT NULL,
                    status TEXT NOT NULL,
                    created_at TEXT DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (restaurant_id) REFERENCES restaurants(id),
                    FOREIGN KEY (table_id) REFERENCES tables(id),
                    FOREIGN KEY (client_id) REFERENCES clients(id)
                );
            """);

            System.out.println("Database initialized.");

        } catch (SQLException e) {
            throw new RuntimeException("Database init failed", e);
        }
    }
}
