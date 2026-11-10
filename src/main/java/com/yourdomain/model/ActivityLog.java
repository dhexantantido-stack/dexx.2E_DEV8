package com.yourdomain.model;

/** [NEW - Deliverable 8] One row of the activity_logs table. */
public record ActivityLog(int logId, int userId, String actionType, String description, String timestamp) { }
