package com.yourdomain.ui;

import com.yourdomain.dao.ActivityLogDao;
import com.yourdomain.dao.UserDao;
import com.yourdomain.model.ActivityLog;
import com.yourdomain.model.User;

import java.util.List;
import java.util.Scanner;

public class AdminMenu {
    private static final String LOG_FORMAT = "%-5s | %-8s | %-18s | %-35s | %-20s%n";
    private static final int DESC_WIDTH = 35;

    private final Scanner scanner;
    private final User admin;
    private final UserDao userDao = new UserDao();
    private final ActivityLogDao logDao = new ActivityLogDao();

    public AdminMenu(Scanner scanner, User admin) {
        this.scanner = scanner;
        this.admin = admin;
    }

    public void show() {
        while (true) {
            System.out.println("\n=== ADMIN MENU ===");
            System.out.println("1. Approve Pending Accounts");
            System.out.println("2. Update User Role / Status");
            System.out.println("3. Record Business Transaction");
            System.out.println("4. View System Audit Logs");   // [UPDATE - Deliverable 8]
            System.out.println("0. Logout");
            System.out.print("Choice: ");
            switch (scanner.nextLine().trim()) {
                case "1" -> approveAccounts();
                case "2" -> updateUser();
                case "3" -> recordDomainTransaction(scanner, admin, logDao);
                case "4" -> auditLogMenu();
                case "0" -> {
                    logDao.logAction(admin.id(), "USER_LOGOUT", admin.username() + " logged out");
                    return;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    // ---------------------------------------------------------------- Deliverable 5
    private void approveAccounts() {
        List<User> pending = userDao.getPendingUsers();
        if (pending.isEmpty()) {
            System.out.println("No pending accounts.");
            return;
        }
        System.out.println("\nPending accounts:");
        for (User u : pending) {
            System.out.printf("ID %d | %s | %s%n", u.id(), u.username(), u.role());
        }
        int id = readInt("Enter User ID to approve (0 to cancel): ");
        if (id == 0) return;
        boolean isPending = pending.stream().anyMatch(u -> u.id() == id);
        if (!isPending) {
            System.out.println("That user is not pending.");
            return;
        }
        if (userDao.updateStatus(id, "APPROVED")) {
            logDao.logAction(admin.id(), "USER_APPROVED",
                    "Admin [" + admin.id() + "] approved account for User ID " + id);
            System.out.println("User " + id + " approved.");
        }
    }

    // ---------------------------------------------------------------- Deliverable 7
    private void updateUser() {
        System.out.println("\nAll users:");
        for (User u : userDao.getAllUsers()) {
            System.out.printf("ID %d | %s | %s | %s%n", u.id(), u.username(), u.role(), u.status());
        }
        int id = readInt("Enter User ID to update (0 to cancel): ");
        if (id == 0) return;
        if (userDao.findById(id).isEmpty()) {
            System.out.println("User not found.");
            return;
        }
        System.out.println("1. Change Role   2. Change Status");
        System.out.print("Choice: ");
        String choice = scanner.nextLine().trim();
        if (choice.equals("1")) {
            System.out.print("New role (ADMIN/TEACHER/STUDENT): ");
            String role = scanner.nextLine().trim().toUpperCase();
            if (!(role.equals("ADMIN") || role.equals("TEACHER") || role.equals("STUDENT"))) {
                System.out.println("Invalid role.");
                return;
            }
            if (userDao.updateRole(id, role)) {
                logDao.logAction(admin.id(), "USER_UPDATED",
                        "Admin [" + admin.id() + "] changed role of User " + id + " to " + role);
                System.out.println("Role updated.");
            }
        } else if (choice.equals("2")) {
            System.out.print("New status (APPROVED/PENDING/DISABLED): ");
            String status = scanner.nextLine().trim().toUpperCase();
            if (!(status.equals("APPROVED") || status.equals("PENDING") || status.equals("DISABLED"))) {
                System.out.println("Invalid status.");
                return;
            }
            if (userDao.updateStatus(id, status)) {
                logDao.logAction(admin.id(), "USER_UPDATED",
                        "Admin [" + admin.id() + "] changed status of User " + id + " to " + status);
                System.out.println("Status updated.");
            }
        } else {
            System.out.println("Invalid choice.");
        }
    }

    // ---------------------------------------------------------------- Domain-specific action
    /**
     * TODO: Replace with YOUR proposal's transaction (e.g. TICKET_CREATED, BILL_PROCESSED,
     * CYLINDER_EXCHANGED, VISITOR_LOGGED). Call logDao.logAction(...) right after the
     * database insert/update of that transaction succeeds.
     */
    static void recordDomainTransaction(Scanner scanner, User user, ActivityLogDao logDao) {
        System.out.print("Transaction details (e.g. reference no. or short note): ");
        String details = scanner.nextLine().trim();
        if (details.isEmpty()) {
            System.out.println("Details cannot be empty.");
            return;
        }
        // ... save the real transaction through its own DAO here ...
        logDao.logAction(user.id(), "TICKET_CREATED",
                "User [" + user.id() + "] created transaction: " + details);
        System.out.println("Transaction recorded and logged.");
    }

    // ---------------------------------------------------------------- Deliverable 8
    private void auditLogMenu() {
        while (true) {
            System.out.println("\n=== SYSTEM AUDIT LOGS ===");
            System.out.println("1. View All System Logs");
            System.out.println("2. Filter Logs by User ID");
            System.out.println("3. Clear Historical Logs");
            System.out.println("0. Back");
            System.out.print("Choice: ");
            switch (scanner.nextLine().trim()) {
                case "1" -> printLogs(logDao.getAllLogs());
                case "2" -> {
                    int userId = readInt("Enter User ID: ");
                    printLogs(logDao.getLogsByUserId(userId));
                }
                case "3" -> clearLogs();
                case "0" -> { return; }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private void printLogs(List<ActivityLog> logs) {
        if (logs.isEmpty()) {
            System.out.println("No log entries found.");
            return;
        }
        String line = "-".repeat(5 + 8 + 18 + 35 + 20 + 12);
        System.out.println();
        System.out.println(line);
        System.out.printf(LOG_FORMAT, "ID", "User ID", "Action", "Description", "Timestamp");
        System.out.println(line);
        for (ActivityLog log : logs) {
            List<String> parts = wrap(log.description(), DESC_WIDTH);
            System.out.printf(LOG_FORMAT, log.logId(), log.userId(), log.actionType(),
                    parts.get(0), log.timestamp());
            for (int i = 1; i < parts.size(); i++) {   // continuation lines keep columns aligned
                System.out.printf(LOG_FORMAT, "", "", "", parts.get(i), "");
            }
        }
        System.out.println(line);
        System.out.println(logs.size() + " log entr" + (logs.size() == 1 ? "y" : "ies") + " shown.");
    }

    /** Splits text into chunks no wider than width, breaking at spaces when possible. */
    private List<String> wrap(String text, int width) {
        List<String> out = new java.util.ArrayList<>();
        String rest = text == null ? "" : text.trim();
        while (rest.length() > width) {
            int cut = rest.lastIndexOf(' ', width);
            if (cut <= 0) cut = width;
            out.add(rest.substring(0, cut).trim());
            rest = rest.substring(cut).trim();
        }
        out.add(rest);
        return out;
    }

    private void clearLogs() {
        System.out.println("WARNING: this permanently deletes ALL audit log entries.");
        System.out.print("Type CLEAR to confirm: ");
        if (!scanner.nextLine().trim().equals("CLEAR")) {
            System.out.println("Cancelled. Nothing was deleted.");
            return;
        }
        int removed = logDao.clearLogs();
        // Leave a trace so the clearing itself stays accountable.
        logDao.logAction(admin.id(), "LOGS_CLEARED",
                "Admin [" + admin.id() + "] cleared " + removed + " log entries");
        System.out.println(removed + " entries deleted.");
    }

    private int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a number.");
            }
        }
    }
}
