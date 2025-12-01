package service;

import model.User;
import model.Book;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class UserService {

private List<User> users;
private User loggedInUser;
private final String FILE_PATH = "src/main/resources/users.txt";

public UserService() {
    users = new ArrayList<>();
    loadUsersFromFile(FILE_PATH);
}

// 🔹 تحميل المستخدمين من الملف
private void loadUsersFromFile(String filePath) {
    try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
        String line;
        while ((line = br.readLine()) != null) {
            if (line.trim().isEmpty()) continue;
            String[] parts = line.split(",");
            if (parts.length >= 3) {
                String username = parts[0].trim();
                String password = parts[1].trim();
                String role = parts[2].trim();
                double fine = 0.0;
                if (parts.length > 3) {
                    try { fine = Double.parseDouble(parts[3]); }
                    catch (NumberFormatException e) { fine = 0.0; }
                }
                User user = new User(username, password, role);
                user.setOutstandingFine(fine);
                users.add(user);
            } else {
                System.out.println("⚠ Skipping corrupted user line: " + line);
            }
        }
    } catch (IOException e) {
        System.out.println("⚠ Error reading users file: " + e.getMessage());
    }
}

// 🔹 تسجيل الدخول بدون إعادة حساب الغرامة
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

// 🔹 تسجيل الخروج
public void logout() {
    if (loggedInUser != null) {
        System.out.println("🔒 " + loggedInUser.getName() + " logged out successfully.");
        loggedInUser = null;
    }
}

public boolean isLoggedIn() { return loggedInUser != null; }
public User getLoggedInUser() { return loggedInUser; }

// ===== إدارة الغرامات =====
public void payFine(User user, double amount, BookService bookService) {
    if (loggedInUser == null || !loggedInUser.equals(user)) {
        System.out.println("❌ Access denied! User must be logged in to pay fine.");
        return;
    }

    double paid = Math.min(amount, user.getOutstandingFine());
    user.setOutstandingFine(user.getOutstandingFine() - paid);

    // إعادة ضبط fineIssued في الكتب إذا دُفعت الغرامة
    if (bookService != null) {
        for (Book b : bookService.getAllBooks()) {
            if (b.isBorrowed() && b.getBorrowedBy() != null
                    && b.getBorrowedBy().getName().equalsIgnoreCase(user.getName())
                    && b.isFineIssued()) {
                b.setFineIssued(false);
            }
        }
        bookService.saveBooksToFile();
    }

    saveUsersToFile();
    System.out.println("✅ Fine paid successfully. Remaining balance: " + user.getOutstandingFine());
}

public void addFine(User user, double amount) {
    if (amount <= 0) return;
    user.setOutstandingFine(user.getOutstandingFine() + amount);
    saveUsersToFile();
    System.out.println("⚠ Fine added to " + user.getName() + ": " + amount + " | Total outstanding: " + user.getOutstandingFine());
}

// ===== عمليات الاستعارة والإرجاع =====
public boolean canBorrow(User user) {
    return user.getOutstandingFine() <= 0;
}

public boolean canReturn(User user) {
    return user.getOutstandingFine() <= 0;
}

public User findUserByName(String username) {
    for (User u : users) {
        if (u.getName().equalsIgnoreCase(username)) return u;
    }
    return null;
}

public List<User> getAllUsers() { return users; }

public void saveUsersToFile() {
    try (PrintWriter pw = new PrintWriter(new FileWriter(FILE_PATH))) {
        for (User u : users) {
            pw.println(u.getName() + "," + u.getPassword() + "," + u.getRole() + "," + u.getOutstandingFine());
        }
    } catch (IOException e) {
        System.out.println("❌ Error saving users file: " + e.getMessage());
    }
}

}
