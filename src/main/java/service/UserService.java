package service;

import model.User;
import model.media;
import java.io.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class UserService {

    private List<User> users;
    private User loggedInUser;
    
    // ✅ 1. جعل المسار متغيراً وليس final
    private String filePath = "src/main/resources/users.txt";

    // =============================================================
    // 2. الكونستركتور الافتراضي (للبرنامج الرئيسي)
    // =============================================================
    public UserService() {
        users = new ArrayList<>();
        loadUsersFromFile();
    }

    // =============================================================
    // 3. كونستركتور مخصص للاختبارات (Test Constructor)
    // ✅ يسمح بتمرير مسار ملف وهمي لكي لا نعدل الملف الأصلي
    // =============================================================
    public UserService(String testFilePath) {
        this.filePath = testFilePath; // استخدام الملف الوهمي
        users = new ArrayList<>();
        loadUsersFromFile();
    }

    // =============================================================
    // 📂 تحميل المستخدمين
    // =============================================================
    private void loadUsersFromFile() {
        // ✅ استخدام this.filePath بدلاً من الثابت
        try (BufferedReader br = new BufferedReader(new FileReader(this.filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                
                String[] parts = line.split(",");
                
                if (parts.length >= 3) {
                    String username = parts[0].trim();
                    String password = parts[1].trim();
                    String role = parts[2].trim();
                    
                    double fine = 0.0;
                    if (parts.length >= 4) {
                        try { 
                            fine = Double.parseDouble(parts[3].trim()); 
                        } catch (NumberFormatException e) { 
                            fine = 0.0; 
                        }
                    }

                    String email = "";
                    if (parts.length >= 5) {
                        email = parts[4].trim();
                    }

                    User user = new User(username, password, role, fine, email);
                    users.add(user);
                }
            }
        } catch (IOException e) {
            System.out.println("⚠ Error reading users file: " + e.getMessage());
        }
    }

    // =============================================================
    // 💾 حفظ المستخدمين
    // =============================================================
    public void saveUsersToFile() {
        // ✅ استخدام this.filePath بدلاً من الثابت
        try (PrintWriter pw = new PrintWriter(new FileWriter(this.filePath))) {
            for (User u : users) {
                pw.println(u.getName() + "," + 
                           u.getPassword() + "," + 
                           u.getRole() + "," + 
                           u.getOutstandingFine() + "," + 
                           u.getEmail());
            }
        } catch (IOException e) {
            System.out.println("❌ Error saving users file: " + e.getMessage());
        }
    }

    // تسجيل الدخول
    public User login(String username, String password, BookService bookService) {
        for (User user : users) {
            if (user.getName().equals(username) && user.getPassword().equals(password)) {
                loggedInUser = user;
                System.out.println("✅ " + username + " logged in successfully as " + user.getRole() + ".");

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

    // =============================================================
    // 💰 دفع الغرامة
    // =============================================================
    public void payFine(User user, double amount, BookService bookService) {
        if (loggedInUser == null || !loggedInUser.equals(user)) {
            System.out.println("❌ Access denied! User must be logged in to pay fine.");
            return;
        }

        System.out.println("\n📊 --- YOUR FINE BREAKDOWN ---");
        boolean hasOverdueItems = false;

        if (bookService != null) {
            for (media m : bookService.getAllBooks()) {
                if (m.isBorrowed() && 
                    m.getBorrowedBy() != null && 
                    m.getBorrowedBy().getName().equalsIgnoreCase(user.getName()) && 
                    m.isOverdue()) {

                    hasOverdueItems = true;
                    long days = ChronoUnit.DAYS.between(m.getDueDate(), LocalDate.now());
                    double itemFine = m.getFineAmount(); 
                    
                    String type = m.getClass().getSimpleName(); 

                    System.out.println(String.format("🔴 [%s] %s | Overdue: %d days | Fine: %.1f NIS", 
                            type, m.getTitle(), days, itemFine));
                }
            }
        }

        if (!hasOverdueItems && user.getOutstandingFine() > 0) {
            System.out.println("⚠ You have unpaid fines from previous returns.");
        }
        
        System.out.println("-------------------------------------");
        System.out.println("💰 Total Outstanding Balance: " + user.getOutstandingFine() + " NIS");
        System.out.println("-------------------------------------\n");

        if (amount <= 0) {
            System.out.println("❌ Invalid amount. Please enter a positive value.");
            return;
        }

        if (amount > user.getOutstandingFine()) {
            System.out.println("❌ Error: You entered " + amount + ", but your fine is only " + user.getOutstandingFine());
            return;
        }

        user.setOutstandingFine(user.getOutstandingFine() - amount);

        if (user.getOutstandingFine() == 0 && bookService != null) {
            boolean itemsReturned = false;
            for (media m : bookService.getAllBooks()) {
                if (m.isBorrowed() &&
                    m.getBorrowedBy() != null &&
                    m.getBorrowedBy().getName().equalsIgnoreCase(user.getName()) &&
                    m.isOverdue()) {

                    m.setBorrowed(false);
                    m.setBorrowedBy(null);
                    m.setDueDate(null);
                    m.setFineIssued(false);

                    System.out.println("📘 Automatically returned: [" + m.getClass().getSimpleName() + "] " + m.getTitle());
                    itemsReturned = true;
                }
            }
            if (itemsReturned) bookService.saveBooksToFile();
        }

        saveUsersToFile(); 
        System.out.println("✅ Payment successful. Remaining balance: " + user.getOutstandingFine());
    }

    public void checkAndApplyFinesForAllUsers(BookService bookService) {
        boolean usersUpdated = false;
        boolean booksUpdated = false;

        for (media m : bookService.getAllBooks()) {
            if (m.isBorrowed() && m.isOverdue() && !m.isFineIssued() && m.getBorrowedBy() != null) {
                User borrower = findUserByName(m.getBorrowedBy().getName());
                if (borrower != null) {
                    double fine = m.getFineAmount();
                    borrower.setOutstandingFine(borrower.getOutstandingFine() + fine);
                    m.setFineIssued(true);
                    usersUpdated = true;
                    booksUpdated = true;
                }
            }
        }
        if (usersUpdated) saveUsersToFile();
        if (booksUpdated) bookService.saveBooksToFile();
    }

    public boolean deleteUser(String username) {
        User userToRemove = null;
        for (User u : users) {
            if (u.getName().equalsIgnoreCase(username)) {
                userToRemove = u;
                break;
            }
        }
        if (userToRemove != null) {
            users.remove(userToRemove);
            saveUsersToFile();
            System.out.println("🗑 User [" + username + "] deleted.");
            return true;
        }
        return false;
    }

    public void addFine(User user, double amount) {
        if (amount <= 0) return;
        user.setOutstandingFine(user.getOutstandingFine() + amount);
        saveUsersToFile();
    }

    public User findUserByName(String username) {
        for (User u : users) {
            if (u.getName().equalsIgnoreCase(username)) return u;
        }
        return null;
    }

    public List<User> getAllUsers() { return users; }
}