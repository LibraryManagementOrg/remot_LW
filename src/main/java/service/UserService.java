package service;

import model.User;
import model.media; // تأكد من أن اسم الكلاس عندك يبدأ بحرف صغير كما أرسلته (media)
import java.io.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
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

    // =============================================================
    // 📂 تحميل المستخدمين (تعديل لقراءة الإيميل)
    // =============================================================
    private void loadUsersFromFile(String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                
                String[] parts = line.split(",");
                
                // التأكد من وجود البيانات الأساسية (الاسم، الباسورد، الرول)
                if (parts.length >= 3) {
                    String username = parts[0].trim();
                    String password = parts[1].trim();
                    String role = parts[2].trim();
                    
                    double fine = 0.0;
                    // قراءة الغرامة (الخانة 4)
                    if (parts.length >= 4) {
                        try { 
                            fine = Double.parseDouble(parts[3].trim()); 
                        } catch (NumberFormatException e) { 
                            fine = 0.0; 
                        }
                    }

                    // ✅ قراءة الإيميل (الخانة 5)
                    String email = "";
                    if (parts.length >= 5) {
                        email = parts[4].trim();
                    }

                    // استخدام الكونستركتور الجديد الذي يحتوي على الإيميل
                    User user = new User(username, password, role, fine, email);
                    users.add(user);
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
    // 💰 دفع الغرامة + تقرير تفصيلي
    // =============================================================
    public void payFine(User user, double amount, BookService bookService) {
        if (loggedInUser == null || !loggedInUser.equals(user)) {
            System.out.println("❌ Access denied! User must be logged in to pay fine.");
            return;
        }

        // 1️⃣ عرض تقرير مفصل للغرامات
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

        // 2️⃣ التحقق من المبلغ المدخل
        if (amount <= 0) {
            System.out.println("❌ Invalid amount. Please enter a positive value.");
            return;
        }

        if (amount > user.getOutstandingFine()) {
            System.out.println("❌ Error: You entered " + amount + ", but your fine is only " + user.getOutstandingFine());
            return;
        }

        // 3️⃣ الخصم
        user.setOutstandingFine(user.getOutstandingFine() - amount);

        // 4️⃣ إرجاع الكتب تلقائياً عند تصفير الدين
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

        saveUsersToFile(); // حفظ التغييرات (بما فيها الإيميل)
        System.out.println("✅ Payment successful. Remaining balance: " + user.getOutstandingFine());
    }

    // تحديث الغرامات
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

    // =============================================================
    // 💾 حفظ المستخدمين (تعديل لحفظ الإيميل)
    // =============================================================
    public void saveUsersToFile() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE_PATH))) {
            for (User u : users) {
                // ✅ إضافة الإيميل في نهاية السطر
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
}