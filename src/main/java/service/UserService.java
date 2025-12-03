package service;

<<<<<<< HEAD
import java.io.*;
=======
import model.User;
import model.media; // تأكد من استيراد Media
import java.io.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
>>>>>>> branch 'master' of https://github.com/layalqaradeh/remot_LW.git
import java.util.ArrayList;
import java.util.List;

import model.User;

public class UserService {

    private List<User> users;
    private User loggedInUser;
    private final String FILE_PATH = "src/main/resources/users.txt";

    public UserService() {
        users = new ArrayList<>();
<<<<<<< HEAD
        loadUsersFromFile();
=======
        loadUsersFromFile(FILE_PATH);
>>>>>>> branch 'master' of https://github.com/layalqaradeh/remot_LW.git
    }

<<<<<<< HEAD
    private void loadUsersFromFile() {
        users.clear(); // تنظيف القائمة قبل التحميل
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
=======
    // تحميل المستخدمين
    private void loadUsersFromFile(String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
>>>>>>> branch 'master' of https://github.com/layalqaradeh/remot_LW.git
            String line;
            while ((line = br.readLine()) != null) {
<<<<<<< HEAD
                User u = User.fromFileString(line);
                if (u != null) {
                    users.add(u);
=======
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
>>>>>>> branch 'master' of https://github.com/layalqaradeh/remot_LW.git
                }
            }
        } catch (IOException e) {
            // الملف قد يكون غير موجود عند أول تشغيل
        }
    }

<<<<<<< HEAD
    // حفظ مستخدم جديد في الملف والقائمة
    public void addUser(User newUser) {
        users.add(newUser);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
            bw.write(newUser.toFileString());
            bw.newLine();
        } catch (IOException e) {
            System.out.println("❌ Error saving user to file!");
        }
    }

    public User login(String username, String password) {
        // تحديث القائمة من الملف قبل اللوجين لضمان وجود المستخدمين الجدد
        loadUsersFromFile(); 
        
=======
    // تسجيل الدخول
    public User login(String username, String password, BookService bookService) {
>>>>>>> branch 'master' of https://github.com/layalqaradeh/remot_LW.git
        for (User user : users) {
            if (user.getName().equals(username) && user.getPassword().equals(password)) {
                loggedInUser = user;
<<<<<<< HEAD
                System.out.println("✅ Logged in as: " + user.getName());
=======
                System.out.println("✅ " + username + " logged in successfully as " + user.getRole() + ".");

                if (bookService != null) {
                    checkAndApplyFinesForAllUsers(bookService);
                }
>>>>>>> branch 'master' of https://github.com/layalqaradeh/remot_LW.git
                return user;
            }
        }
        return null;
    }

    public void logout() {
        loggedInUser = null;
        System.out.println("🔒 Logged out.");
    }

    public boolean isLoggedIn() { return loggedInUser != null; }
    public User getLoggedInUser() { return loggedInUser; }

<<<<<<< HEAD
    public User getLoggedInUser() {
        return loggedInUser;
    }

    public User findUserByName(String name) {
        loadUsersFromFile(); // تحديث
=======
    // =============================================================
    // 💰 دفع الغرامة + تقرير تفصيلي (US5.3 Mixed Media Handling)
    // =============================================================
    public void payFine(User user, double amount, BookService bookService) {
        if (loggedInUser == null || !loggedInUser.equals(user)) {
            System.out.println("❌ Access denied! User must be logged in to pay fine.");
            return;
        }

        // 1️⃣ عرض تقرير مفصل للغرامات (كتب vs سيديات)
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
                    double itemFine = m.getFineAmount(); // يستخدم الاستراتيجية (10 للكتاب، 20 للسي دي)
                    
                    // تحديد النوع للطباعة (Book أو CD)
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

        saveUsersToFile();
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
>>>>>>> branch 'master' of https://github.com/layalqaradeh/remot_LW.git
        for (User u : users) {
<<<<<<< HEAD
            if (u.getName().equalsIgnoreCase(name)) return u;
=======
            if (u.getName().equalsIgnoreCase(username)) {
                userToRemove = u;
                break;
            }
>>>>>>> branch 'master' of https://github.com/layalqaradeh/remot_LW.git
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

<<<<<<< HEAD
    // دفع الغرامة وتحديث الملف
    public void payFine(User user, double amount) {
        if (amount <= 0) return;
        
        double newFine = Math.max(0, user.getOutstandingFine() - amount);
        user.setOutstandingFine(newFine);
        updateUserFile(); // تحديث الملف لحفظ الغرامة الجديدة
        System.out.println("✅ Payment successful. Remaining fine: " + newFine);
    }
    
    // إعادة كتابة الملف بالكامل (لتحديث الغرامات مثلاً)
    private void updateUserFile() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (User u : users) {
                bw.write(u.toFileString());
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("❌ Error updating users file!");
=======
    public List<User> getAllUsers() { return users; }

    public void saveUsersToFile() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE_PATH))) {
            for (User u : users) {
                pw.println(u.getName() + "," + u.getPassword() + "," + u.getRole() + "," + u.getOutstandingFine());
            }
        } catch (IOException e) {
            System.out.println("❌ Error saving users file: " + e.getMessage());
>>>>>>> branch 'master' of https://github.com/layalqaradeh/remot_LW.git
        }
    }
}