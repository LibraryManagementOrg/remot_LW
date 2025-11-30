package service;

import java.util.List;

import model.Book;
import model.User;

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

        List<Book> allBooks = bookService.getAllBooks();

        if (allBooks.isEmpty()) {
            System.out.println("No books available.");
            return;
        }

        System.out.println("📚 All Books:");
        for (Book b : allBooks) {
            System.out.println(b);
        }
    }

    // --- إرجاع المستخدم الحالي ---
    public User getCurrentUser() {
        return currentUser;
    }
}
