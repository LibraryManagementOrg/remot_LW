package service;

import model.User;
import model.media; // ✅ استخدام Media بدلاً من Book

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

    // ========================================================
    // 🌟 عرض الوسائط المتأخرة (كتب + CDs) - تعديل Sprint 5
    // ========================================================
    public void showOverdueBooks(List<media> items) { // ✅ تغيير المدخلات إلى List<Media>
        boolean found = false;
        System.out.println("📋 Overdue Items (Books & CDs):");
        
        for (media m : items) { // ✅ التكرار على Media
            if (m.isOverdue() && m.getBorrowedBy() != null) {
                found = true;
                long daysOverdue = ChronoUnit.DAYS.between(m.getDueDate(), LocalDate.now());
                
                // m.getClass().getSimpleName() ستطبع إما "Book" أو "CD"
                String type = m.getClass().getSimpleName();
                
                System.out.println("[" + type + "] " + m.getTitle() + 
                        " | Borrowed by: " + m.getBorrowedBy().getName() + 
                        " | Due: " + m.getDueDate() + 
                        " | Days overdue: " + daysOverdue);
            }
        }
        
        if (!found) {
            System.out.println("✅ No overdue items at the moment.");
        }
    }

    // ========================================================
    // عرض الغرامات الحالية
    // ========================================================
    // ✅ يجب تغيير التوقيع ليقبل List<Media> ليتوافق مع BookService حتى لو لم نستخدم القائمة هنا
    public void issueFines(List<media> items, UserService userService) {
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