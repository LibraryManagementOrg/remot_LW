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

    // تحميل المستخدمين من الملف
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

    // تسجيل الدخول
    public User login(String username, String password, BookService bookService) {
        for (User user : users) {
            if (user.getName().equals(username) && user.getPassword().equals(password)) {
                loggedInUser = user;
                System.out.println("✅ " + username + " logged in successfully as " + user.getRole() + ".");

                // تحديث شامل للغرامات عند دخول أي شخص
                if (bookService != null) {
                    checkAndApplyFinesForAllUsers(bookService);
                }

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

    public boolean isLoggedIn() { return loggedInUser != null; }
    public User getLoggedInUser() { return loggedInUser; }


    // ===== دفع الغرامة =====
    public void payFine(User user, double amount, BookService bookService) {
        if (loggedInUser == null || !loggedInUser.equals(user)) {
            System.out.println("❌ Access denied! User must be logged in to pay fine.");
            return;
        }

        if (amount <= 0) {
            System.out.println("❌ Invalid amount.");
            return;
        }

        if (amount > user.getOutstandingFine()) {
            System.out.println("❌ Error: You cannot pay more than the outstanding fine (" + user.getOutstandingFine() + ")");
            return;
        }

        user.setOutstandingFine(user.getOutstandingFine() - amount);

        // إرجاع الكتب فقط عند تصفير الدين بالكامل
        if (user.getOutstandingFine() == 0 && bookService != null) {
            boolean booksReturned = false;
            for (Book b : bookService.getAllBooks()) {
                if (b.isBorrowed() &&
                    b.getBorrowedBy() != null &&
                    b.getBorrowedBy().getName().equalsIgnoreCase(user.getName()) &&
                    b.isOverdue()) {

                    b.setBorrowed(false);
                    b.setBorrowedBy(null);
                    b.setDueDate(null);
                    b.setFineIssued(false); // إعادة التعيين لأن الكتاب رجع

                    System.out.println("📘 Returned overdue book automatically: " + b.getTitle());
                    booksReturned = true;
                }
            }
            if (booksReturned) bookService.saveBooksToFile();
        }

        saveUsersToFile();
        System.out.println("✅ Fine paid successfully. Remaining balance: " + user.getOutstandingFine());
    }


    // ===== 🌟 تحديث الغرامات لجميع المستخدمين دفعة واحدة 🌟 =====
    public void checkAndApplyFinesForAllUsers(BookService bookService) {
        boolean usersUpdated = false;
        boolean booksUpdated = false;

        for (Book b : bookService.getAllBooks()) {
            if (b.isBorrowed() 
                && b.isOverdue() 
                && !b.isFineIssued() 
                && b.getBorrowedBy() != null) {

                User borrower = findUserByName(b.getBorrowedBy().getName());

                if (borrower != null) {
                    double fine = b.getFineAmount();
                    borrower.setOutstandingFine(borrower.getOutstandingFine() + fine);
                    b.setFineIssued(true);

                    usersUpdated = true;
                    booksUpdated = true;
                }
            }
        }

        if (usersUpdated) saveUsersToFile();
        if (booksUpdated) bookService.saveBooksToFile();
    }


    // ==========================================
    // 🗑️ دالة حذف المستخدم (Unregister User) - جديد
    // ==========================================
    public boolean deleteUser(String username) {
        User userToRemove = null;
        
        // البحث عن المستخدم
        for (User u : users) {
            if (u.getName().equalsIgnoreCase(username)) {
                userToRemove = u;
                break;
            }
        }

        // الحذف والحفظ
        if (userToRemove != null) {
            users.remove(userToRemove);
            saveUsersToFile();
            System.out.println("🗑 User [" + username + "] has been permanently deleted.");
            return true;
        } else {
            // لا نطبع خطأ هنا، نترك التحكم لـ AdminService
            return false;
        }
    }


    public void addFine(User user, double amount) {
        if (amount <= 0) return;
        user.setOutstandingFine(user.getOutstandingFine() + amount);
        saveUsersToFile();
        System.out.println("⚠ Fine added to " + user.getName() + ": " + amount +
                " | Total outstanding: " + user.getOutstandingFine());
    }

    public boolean canBorrow(User user) { return user.getOutstandingFine() <= 0; }
    public boolean canReturn(User user) { return user.getOutstandingFine() <= 0; }

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
                // ملاحظة: إذا قمت بدمج الإيميل لاحقاً، تذكر تعديل هذا السطر ليحفظ الإيميل أيضاً
                pw.println(u.getName() + "," + u.getPassword() + "," + u.getRole() + "," + u.getOutstandingFine());
            }
        } catch (IOException e) {
            System.out.println("❌ Error saving users file: " + e.getMessage());
        }
    }
}