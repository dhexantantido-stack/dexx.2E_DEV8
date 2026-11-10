package com.yourdomain.ui;

import com.yourdomain.dao.ActivityLogDao;
import com.yourdomain.dao.UserDao;
import com.yourdomain.model.User;

import java.util.Optional;
import java.util.Scanner;

public class Authentication {
    private final Scanner scanner = new Scanner(System.in);
    private final UserDao userDao = new UserDao();
    private final ActivityLogDao logDao = new ActivityLogDao();

    public void start() {
        while (true) {
            System.out.println("\n=== STUDENT CONSOLE APP ===");
            System.out.println("1. Login");
            System.out.println("2. Register");
            System.out.println("0. Exit");
            System.out.print("Choice: ");
            switch (scanner.nextLine().trim()) {
                case "1" -> login();
                case "2" -> register();
                case "0" -> {
                    System.out.println("Goodbye!");
                    return;
                }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private void register() {
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();
        System.out.print("Role (TEACHER/STUDENT): ");
        String role = scanner.nextLine().trim().toUpperCase();

        if (username.isEmpty() || password.isEmpty() || !(role.equals("TEACHER") || role.equals("STUDENT"))) {
            System.out.println("Invalid input. Role must be TEACHER or STUDENT.");
            return;
        }
        int newId = userDao.register(username, password, role);
        if (newId > 0) {
            logDao.logAction(newId, "USER_REGISTERED",
                    "User " + username + " registered as " + role + " (PENDING)");
            System.out.println("Registered! Your account is PENDING admin approval.");
        }
    }

    private void login() {
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();

        Optional<User> found = userDao.login(username, password);
        if (found.isEmpty()) {
            System.out.println("Invalid username or password.");
            return;
        }
        User user = found.get();
        if (!user.status().equals("APPROVED")) {
            System.out.println("Your account is " + user.status() + ". Please wait for admin approval.");
            return;
        }
        logDao.logAction(user.id(), "USER_LOGIN", user.username() + " logged in as " + user.role());
        System.out.println("Welcome, " + user.username() + " (" + user.role() + ")!");

        if (user.role().equals("ADMIN")) {
            new AdminMenu(scanner, user).show();
        } else {
            userMenu(user);
        }
    }

    private void userMenu(User user) {
        while (true) {
            System.out.println("\n--- " + user.role() + " MENU ---");
            System.out.println("1. Record Business Transaction");
            System.out.println("0. Logout");
            System.out.print("Choice: ");
            String choice = scanner.nextLine().trim();
            if (choice.equals("1")) {
                AdminMenu.recordDomainTransaction(scanner, user, logDao);
            } else if (choice.equals("0")) {
                logDao.logAction(user.id(), "USER_LOGOUT", user.username() + " logged out");
                return;
            } else {
                System.out.println("Invalid choice.");
            }
        }
    }
}
