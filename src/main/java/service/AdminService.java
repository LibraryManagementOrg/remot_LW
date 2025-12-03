package service;

import java.util.List;
import model.User;
import model.media;

public class AdminService {

    private boolean loggedIn = false; // حالة دخول الأدمن
    private User currentUser = null;  // المستخدم الحالي

    // --- تسجيل دخول الأدمن باستخدام كائن User ---
    public void loginAdmin(User user) {
        if (user != null && "Admin".equalsIgnoreCase(user.getRole())) {
            loggedIn = true;
            currentUser = user;
            System.out.println("✅ Admin session started for " + user.getName());
        } else {
            System.out.println("❌ Access denied! Not an admin.");
        }
    }

    // --- تسجيل الخروج ---
    public void logout() {
        loggedIn = false;
        currentUser = null;
        System.out.println("🔒 Admin logged out successfully.");
    }

    // --- التحقق من حالة تسجيل الدخول ---
    public boolean isLoggedIn() {
        return loggedIn;
    }

    // --- عرض جميع الكتب ---
    public void showAllBooks(BookService bookService) {
        if (!loggedIn) {
            System.out.println("❌ Access denied! Please log in as admin.");
            return;
        }

        List<media> allBooks = bookService.getAllBooks();

        if (allBooks.isEmpty()) {
            System.out.println("No books available.");
            return;
        }

        System.out.println("📚 All Books:");
        for (media b : allBooks) {
            System.out.println(b);
        }
    }

    // =========================================================
    // 🛑 US4.2: إلغاء تسجيل المستخدم (Unregister User)
    // =========================================================
    public void unregisterUser(String username, UserService userService, BookService bookService) {
        // 1. التحقق من صلاحية الأدمن
        if (!loggedIn) {
            System.out.println("❌ Access denied! Only admins can unregister users.");
            return;
        }

        // 2. البحث عن المستخدم
        User user = userService.findUserByName(username);
        if (user == null) {
            System.out.println("❌ User not found: " + username);
            return;
        }

        // 3. التحقق من الغرامات (Condition: No unpaid fines)
        if (user.getOutstandingFine() > 0) {
            System.out.println("⛔ Cannot delete user! They have unpaid fines: " + user.getOutstandingFine());
            return;
        }

        // 4. التحقق من الكتب المستعارة (Condition: No active loans)
        boolean hasActiveLoans = false;
        for (media b : bookService.getAllBooks()) {
            if (b.isBorrowed() && 
                b.getBorrowedBy() != null && 
                b.getBorrowedBy().getName().equalsIgnoreCase(username)) {
                hasActiveLoans = true;
                break;
            }
        }

        if (hasActiveLoans) {
            System.out.println("⛔ Cannot delete user! They still have borrowed books.");
            return;
        }

        // 5. إذا تجاوز كل الشروط، قم بالحذف
        boolean deleted = userService.deleteUser(username);
        if (deleted) {
            System.out.println("✅ User [" + username + "] unregistered successfully.");
        }
    }
    
    // =========================================================
    // 📧 US3.1: إرسال تنبيهات (Observer Pattern Applied) ✅
    // =========================================================
    public void sendOverdueReminders(UserService userService, BookService bookService) {
        if (!loggedIn) {
            System.out.println("❌ Access denied! Please log in as admin.");
            return;
        }

        System.out.println("📧 Initiating notification process...");

        // 1. إنشاء الـ Observer (الذي يعرف كيف يرسل الإيميل الحقيقي)
        // تأكدي أن RealEmailService يحتوي على إعدادات Gmail الصحيحة
        NotificationObserver emailObserver = new RealEmailService();

        // 2. إنشاء الـ Subject/Logic Service وحقن الـ Observer فيه
        ReminderService reminderService = new ReminderService(emailObserver, userService);

        // 3. تنفيذ العملية (ReminderService سيقوم بالفحص واستدعاء Observer عند الحاجة)
        reminderService.sendOverdueReminders(bookService.getAllBooks());
    }

    // --- إرجاع المستخدم الحالي ---
    public User getCurrentUser() {
        return currentUser;
    }
}