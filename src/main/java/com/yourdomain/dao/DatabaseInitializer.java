package com.yourdomain.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    private static final String CREATE_USERS = """
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT NOT NULL UNIQUE,
                password TEXT NOT NULL,
                role TEXT NOT NULL DEFAULT 'STUDENT',
                status TEXT NOT NULL DEFAULT 'PENDING'
            )
            """;

    // [UPDATE - Deliverable 8] Audit trail table
    private static final String CREATE_ACTIVITY_LOGS = """
            CREATE TABLE IF NOT EXISTS activity_logs (
                log_id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                action_type TEXT NOT NULL,
                description TEXT NOT NULL,
                timestamp DATETIME DEFAULT (datetime('now', 'localtime'))
            )
            """;

    private DatabaseInitializer() { }

    public static void initialize() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement()) {
            st.execute(CREATE_USERS);
            st.execute(CREATE_ACTIVITY_LOGS);
            seedDefaultAdmin(conn);
        } catch (SQLException e) {
            System.out.println("Database initialization failed: " + e.getMessage());
        }
    }

    /** Creates admin / admin123 (APPROVED) only when no admin exists yet. */
    private static void seedDefaultAdmin(Connection conn) throws SQLException {
        try (PreparedStatement check = conn.prepareStatement(
                "SELECT COUNT(*) FROM users WHERE role = 'ADMIN'");
             ResultSet rs = check.executeQuery()) {
            if (rs.next() && rs.getInt(1) == 0) {
                try (PreparedStatement ins = conn.prepareStatement(
                        "INSERT INTO users (username, password, role, status) VALUES (?, ?, 'ADMIN', 'APPROVED')")) {
                    ins.setString(1, "admin");
                    ins.setString(2, UserDao.hash("admin123"));
                    ins.executeUpdate();
                }
            }
        }
    }
}
