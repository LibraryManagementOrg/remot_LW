package service;

import model.User;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UserService {
    private List<User> users;
    private User loggedInUser;

    public UserService() {
        users = new ArrayList<>();
        loadUsersFromFile("src/main/resources/users.txt"); // قراءة كل المستخدمين من الملف
    }

    private void loadUsersFromFile(String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 3) {
                    String username = parts[0].trim();
                    String password = parts[1].trim();
                    String role = parts[2].trim();
                    users.add(new User(username, password, role));
                }
            }
        } catch (IOException e) {
            System.out.println("⚠ Error reading users file: " + e.getMessage());
        }
    }

    // تسجيل الدخول وإرجاع كائن المستخدم مهما كان دوره
    public User login(String username, String password) {
        for (User user : users) {
            if (user.getName().equals(username) && user.getPassword().equals(password)) {
                loggedInUser = user;
                System.out.println("✅ " + username + " logged in successfully as " + user.getRole() + ".");
                return user;
            }
        }
        System.out.println("❌ Invalid username or password.");
        return null;
    }

    public void logout() {
        if (loggedInUser != null) {
            System.out.println("🔒 " + loggedInUser.getName() + " logged out successfully.");
            loggedInUser = null;
        }
    }

    public boolean isLoggedIn() {
        return loggedInUser != null;
    }

    public User getLoggedInUser() {
        return loggedInUser;
    }

    // ===== إدارة الغرامات =====

    // دفع مبلغ من الغرامة (يمكن للمستخدم فقط)
    public void payFine(User user, double amount) {
        if (loggedInUser == null || !loggedInUser.equals(user)) {
            System.out.println("❌ Access denied! User must be logged in to pay fine.");
            return;
        }

        double remaining = user.getOutstandingFine() - amount;
        user.setOutstandingFine(Math.max(0, remaining));
        System.out.println("✅ Fine paid successfully. Remaining balance: " + user.getOutstandingFine());
    }

    // إضافة غرامة لأي مستخدم (يمكن أن يستخدمها Librarian عند التأخير)
    public void addFine(User user, double amount) {
        user.setOutstandingFine(user.getOutstandingFine() + amount);
        System.out.println("⚠ Fine added to " + user.getName() + ": " + amount + " | Total outstanding: " + user.getOutstandingFine());
    }

    // البحث عن مستخدم حسب الاسم
    public User findUserByName(String username) {
        for (User u : users) {
            if (u.getName().equalsIgnoreCase(username)) {
                return u;
            }
        }
        return null;
    }
    

    // جلب كل المستخدمين (مفيد للعرض والإدارة)
    public List<User> getAllUsers() {
        return users;
    }
}