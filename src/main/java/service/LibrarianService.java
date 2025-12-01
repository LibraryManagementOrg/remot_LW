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

public boolean isLoggedIn() { return loggedInLibrarian != null; }

// عرض الكتب المتأخرة بدون تغيير الغرامة
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

// عرض الغرامات الحالية من الملف بدون إضافة جديدة
public void issueFines(List<Book> books, UserService userService) {
    System.out.println("📋 Current fines (from users file):");
    boolean hasFines = false;
    for (User u : userService.getAllUsers()) {
        if (u.getOutstandingFine() > 0) {
            hasFines = true;
            System.out.println("User: " + u.getName() + " | Outstanding fine: " + u.getOutstandingFine());
        }
    }
    if (!hasFines) {
        System.out.println("✅ No outstanding fines at the moment.");
    }
}


}