package servicetest;


import model.User;
import model.media;
import service.LibrarianService;
import service.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LibrarianServiceTest {

    private LibrarianService librarianService;
    private User librarianUser;
    private User regularUser;

    @BeforeEach
    void setUp() {
        librarianService = new LibrarianService();
        librarianUser = new User("Sarah", "pass", "Librarian");
        regularUser = new User("John", "pass", "User");
    }

    @Test
    @DisplayName("1. Test Login Logic (Role Verification)")
    void testLoginLogout() {
        // 1. الحالة المبدئية
        assertFalse(librarianService.isLoggedIn());

        // 2. محاولة دخول مستخدم عادي (يجب أن تفشل)
        librarianService.loginLibrarian(regularUser);
        assertFalse(librarianService.isLoggedIn(), "Regular user should not be able to login as Librarian");

        // 3. محاولة دخول أمين مكتبة (يجب أن تنجح)
        librarianService.loginLibrarian(librarianUser);
        assertTrue(librarianService.isLoggedIn(), "Librarian should be able to login");

        // 4. تسجيل الخروج
        librarianService.logout();
        assertFalse(librarianService.isLoggedIn());
    }

    @Test
    @DisplayName("2. Test Show Overdue Books (Execution Flow)")
    void testShowOverdueBooks() {
        // تجهيز قائمة وهمية من الميديا (Stubbing Anonymous Class)
        List<media> items = new ArrayList<>();

        // العنصر 1: متأخر
        media overdueItem = new media("Late Book", "Author", "1") {
            public int getLoanPeriod() { return 10; }
            public double getDailyFine() { return 1.0; }
        };
        overdueItem.setBorrowed(true);
        overdueItem.setBorrowedBy(regularUser);
        overdueItem.setDueDate(LocalDate.now().minusDays(5)); // متأخر 5 أيام
        items.add(overdueItem);

        // العنصر 2: غير متأخر
        media onTimeItem = new media("On Time CD", "Artist", "2") {
            public int getLoanPeriod() { return 7; }
            public double getDailyFine() { return 2.0; }
        };
        onTimeItem.setBorrowed(true);
        onTimeItem.setBorrowedBy(regularUser);
        onTimeItem.setDueDate(LocalDate.now().plusDays(5)); // في المستقبل
        items.add(onTimeItem);

        // بما أن الدالة void وتطبع على الشاشة، نتأكد فقط أنها لا ترمي Exception
        // وأنها تعالج القائمة بشكل سليم
        assertDoesNotThrow(() -> librarianService.showOverdueBooks(items));
        
        // سيناريو قائمة فارغة
        assertDoesNotThrow(() -> librarianService.showOverdueBooks(new ArrayList<>()));
    }

    @Test
    @DisplayName("3. Test Issue Fines (Execution Flow)")
    void testIssueFines() {
        // تجهيز UserService وهمي يرجع قائمة مستخدمين محددة
        UserService stubUserService = new UserService() {
            @Override
            public List<User> getAllUsers() {
                List<User> users = new ArrayList<>();
                
                User u1 = new User("UserWithFine", "1", "User");
                u1.setOutstandingFine(50.0);
                users.add(u1);

                User u2 = new User("CleanUser", "2", "User");
                u2.setOutstandingFine(0.0);
                users.add(u2);
                
                return users;
            }
        };

        // الدالة تحتاج List<media> لكنها لا تستخدمها فعلياً في الكود الذي أرسلته
        // ومع ذلك نمرر قائمة فارغة للالتزام بالتوقيع
        List<media> dummyItems = new ArrayList<>();

        // التحقق من أن الدالة تعمل دون مشاكل
        assertDoesNotThrow(() -> librarianService.issueFines(dummyItems, stubUserService));
    }
}