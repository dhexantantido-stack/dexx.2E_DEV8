package com.yourdomain.dao;

import com.yourdomain.model.ActivityLog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** [NEW - Deliverable 8] DAO for inserting and fetching audit trail entries. */
public class ActivityLogDao {

    /** Inserts one audit entry. Never throws: a logging failure must not break the business action. */
    public void logAction(int userId, String actionType, String description) {
        String sql = "INSERT INTO activity_logs (user_id, action_type, description) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, actionType);
            ps.setString(3, description);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("[Audit] Could not write log: " + e.getMessage());
        }
    }

    public List<ActivityLog> getAllLogs() {
        String sql = "SELECT log_id, user_id, action_type, description, timestamp "
                + "FROM activity_logs ORDER BY timestamp DESC, log_id DESC";
        List<ActivityLog> logs = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) logs.add(map(rs));
        } catch (SQLException e) {
            System.out.println("[Audit] Could not read logs: " + e.getMessage());
        }
        return logs;
    }

    public List<ActivityLog> getLogsByUserId(int userId) {
        String sql = "SELECT log_id, user_id, action_type, description, timestamp "
                + "FROM activity_logs WHERE user_id = ? ORDER BY timestamp DESC, log_id DESC";
        List<ActivityLog> logs = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) logs.add(map(rs));
            }
        } catch (SQLException e) {
            System.out.println("[Audit] Could not read logs: " + e.getMessage());
        }
        return logs;
    }

    /** Deletes all historical logs. Returns the number of rows removed. */
    public int clearLogs() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement st = conn.createStatement()) {
            return st.executeUpdate("DELETE FROM activity_logs");
        } catch (SQLException e) {
            System.out.println("[Audit] Could not clear logs: " + e.getMessage());
            return 0;
        }
    }

    private ActivityLog map(ResultSet rs) throws SQLException {
        return new ActivityLog(rs.getInt("log_id"), rs.getInt("user_id"),
                rs.getString("action_type"), rs.getString("description"), rs.getString("timestamp"));
    }
}
