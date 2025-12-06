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
    // تم التعديل: يعيد String بدلاً من void
    public String logout() {
        if (loggedIn) {
            String msg = "🔒 Admin logged out successfully.";
            loggedIn = false;
            currentUser = null;
            return msg;
        }
        return "Admin was not logged in.";
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
    // 🔴 تم التعديل: يعيد String بدلاً من void
    // =========================================================
    public String unregisterUser(String username, UserService userService, BookService bookService) {
        // 1. التحقق من صلاحية الأدمن
        if (!loggedIn) {
            return "❌ Access denied! Only admins can unregister users.";
        }

        // 2. البحث عن المستخدم
        User user = userService.findUserByName(username);
        if (user == null) {
            return "❌ User not found: " + username;
        }

        // 3. التحقق من الغرامات (Condition: No unpaid fines)
        if (user.getOutstandingFine() > 0) {
            return "⛔ Cannot delete user! They have unpaid fines: " + user.getOutstandingFine();
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
            return "⛔ Cannot delete user! They still have borrowed books.";
        }

    
        if (userService.deleteUser(username).startsWith("🗑")) { // افتراض أن deleteUser أصبحت تعيد رسالة
             return "✅ User [" + username + "] unregistered successfully.";
        }
        
        // إذا كانت لا تزال تعيد boolean (يجب التأكد من UserService.java)
        /*
        boolean deleted = userService.deleteUser(username);
        if (deleted) {
            return "✅ User [" + username + "] unregistered successfully.";
        }
        */
        
        return "❌ Failed to complete unregistration process."; // رسالة افتراضية
    }

    public String sendOverdueReminders(UserService userService, BookService bookService) {
        if (!loggedIn) {
            return "❌ Access denied! Please log in as admin.";
        }

        // 1. إنشاء الـ Observer
        NotificationObserver emailObserver = new RealEmailService();

        // 2. إنشاء الـ Subject/Logic Service وحقن الـ Observer فيه
        ReminderService reminderService = new ReminderService(emailObserver, userService);

        // 3. تنفيذ العملية
        int count = reminderService.sendOverdueReminders(bookService.getAllBooks());
        
        return String.format("📧 Notification process initiated. %d reminders were sent.", count);
    }

    // --- إرجاع المستخدم الحالي ---
    public User getCurrentUser() {
        return currentUser;
    }
}