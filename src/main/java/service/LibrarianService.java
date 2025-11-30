package service;

import model.User;
import model.Book;
import java.time.temporal.ChronoUnit;
import java.time.LocalDate;
import java.util.List;

public class LibrarianService {
    private User loggedInLibrarian;

    // تسجيل دخول أمين المكتبة
    public void loginLibrarian(User librarian) {
        if (!"Librarian".equalsIgnoreCase(librarian.getRole())) {
            System.out.println("❌ Not a librarian!");
            return;
        }
        loggedInLibrarian = librarian;
        System.out.println("✅ Librarian session started for " + librarian.getName());
    }

    // تسجيل خروج أمين المكتبة
    public void logout() {
        if (loggedInLibrarian != null) {
            System.out.println("🔒 Librarian logged out successfully.");
            loggedInLibrarian = null;
        }
    }

    public boolean isLoggedIn() {
        return loggedInLibrarian != null;
    }

    // عرض الكتب المتأخرة
    public void showOverdueBooks(List<Book> books) {
        boolean found = false;
        System.out.println("📋 Overdue Books:");
        for (Book b : books) {
            if (b.isOverdue() && b.getBorrowedBy() != null) {
                found = true;
                long daysOverdue = ChronoUnit.DAYS.between(b.getDueDate(), LocalDate.now());
                System.out.println(b.getTitle() + " | Borrowed by: " 
                    + b.getBorrowedBy().getName() 
                    + " | Due: " + b.getDueDate()
                    + " | Days overdue: " + daysOverdue);
            }
        }
        if (!found) {
            System.out.println("✅ No overdue books at the moment.");
        }
    }

    // إصدار الغرامة تلقائياً حسب عدد الأيام المتأخرة
    public void issueFines(List<Book> books, UserService userService) {
        boolean finesIssued = false;

        for (Book b : books) {
            if (b.isOverdue() && b.getBorrowedBy() != null) {
                // نجيب اسم المستخدم
                String username = b.getBorrowedBy().getName();
                
                // نجيب كائن المستخدم الحقيقي من UserService
                User realUser = userService.findUserByName(username);
                
                if (realUser != null) {
                    long daysOverdue = ChronoUnit.DAYS.between(b.getDueDate(), LocalDate.now());
                    double fine = daysOverdue * 1.0; // غرامة لكل يوم
                    userService.addFine(realUser, fine); // نضيف الغرامة للمستخدم الحقيقي
                    System.out.println("⚠ Fine issued to " + realUser.getName() 
                        + ": " + fine + " (Days overdue: " + daysOverdue + ")");
                    finesIssued = true;
                }
            }
        }

        if (!finesIssued) {
            System.out.println("✅ No fines to issue. All borrowed books are within due dates.");
        }
    }
}
