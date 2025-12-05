package servicetest;

import model.Book;
import model.User;
import model.media;
import service.AdminService;
import service.BookService;
import service.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AdminServiceTest {

    private AdminService adminService;
    private User adminUser;
    private User regularUser;
    
    // لالتقاط ما يتم طباعته على الشاشة للتحقق من الرسائل
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();

    @BeforeEach
    void setUp() {
        adminService = new AdminService();
        adminUser = new User("SuperAdmin", "123", "Admin");
        regularUser = new User("JohnDoe", "pass", "User");
        
        // توجيه System.out لالتقاط الرسائل
        System.setOut(new PrintStream(outContent));
    }

    // =================================================================
    // 1. Test Login/Logout Logic
    // =================================================================
    
    @Test
    @DisplayName("Login: Success as Admin")
    void testLoginSuccess() {
        adminService.loginAdmin(adminUser);
        assertTrue(adminService.isLoggedIn());
        assertEquals(adminUser, adminService.getCurrentUser());
        assertTrue(outContent.toString().contains("Admin session started"));
    }

    @Test
    @DisplayName("Login: Fail as Regular User")
    void testLoginFailRegularUser() {
        adminService.loginAdmin(regularUser);
        assertFalse(adminService.isLoggedIn());
        assertTrue(outContent.toString().contains("Access denied"));
    }

    @Test
    @DisplayName("Login: Fail as Null")
    void testLoginFailNull() {
        adminService.loginAdmin(null);
        assertFalse(adminService.isLoggedIn());
        assertTrue(outContent.toString().contains("Access denied"));
    }

    @Test
    @DisplayName("Logout: Success")
    void testLogout() {
        adminService.loginAdmin(adminUser);
        adminService.logout();
        assertFalse(adminService.isLoggedIn());
        assertNull(adminService.getCurrentUser());
        assertTrue(outContent.toString().contains("logged out successfully"));
    }

    // =================================================================
    // 2. Test showAllBooks
    // =================================================================

    @Test
    @DisplayName("Show Books: Fail (Not Logged In)")
    void testShowBooksNotLoggedIn() {
        adminService.showAllBooks(null);
        assertTrue(outContent.toString().contains("Access denied"));
    }

    @Test
    @DisplayName("Show Books: Empty List")
    void testShowBooksEmpty() {
        adminService.loginAdmin(adminUser);
        
        // Stubbing BookService باستخدام ملف وهمي لتجنب الأخطاء
        BookService stubBookSrv = new BookService(null, null, "dummy.txt") {
            @Override
            public List<media> getAllBooks() {
                return Collections.emptyList();
            }
        };

        adminService.showAllBooks(stubBookSrv);
        assertTrue(outContent.toString().contains("No books available"));
    }

    @Test
    @DisplayName("Show Books: List with Items")
    void testShowBooksWithItems() {
        adminService.loginAdmin(adminUser);
        
        BookService stubBookSrv = new BookService(null, null, "dummy.txt") {
            @Override
            public List<media> getAllBooks() {
                List<media> list = new ArrayList<>();
                list.add(new Book("TestBook", "Auth", "123"));
                return list;
            }
        };

        adminService.showAllBooks(stubBookSrv);
        assertTrue(outContent.toString().contains("TestBook"));
    }

    // =================================================================
    // 3. Test unregisterUser (All Branches)
    // =================================================================

    @Test
    @DisplayName("Unregister: Fail (Not Logged In)")
    void testUnregisterNotLoggedIn() {
        adminService.unregisterUser("any", null, null);
        assertTrue(outContent.toString().contains("Access denied"));
    }

    @Test
    @DisplayName("Unregister: Fail (User Not Found)")
    void testUnregisterUserNotFound() {
        adminService.loginAdmin(adminUser);
        
        // Stubbing UserService باستخدام ملف وهمي
        UserService stubUserSrv = new UserService("dummy_users.txt") {
            @Override
            public User findUserByName(String name) {
                return null; // لا يوجد مستخدم
            }
        };

        adminService.unregisterUser("Ghost", stubUserSrv, null);
        assertTrue(outContent.toString().contains("User not found"));
    }

    @Test
    @DisplayName("Unregister: Fail (User Has Fines)")
    void testUnregisterUserHasFines() {
        adminService.loginAdmin(adminUser);
        
        UserService stubUserSrv = new UserService("dummy_users.txt") {
            @Override
            public User findUserByName(String name) {
                User u = new User("Debtor", "pass", "User");
                u.setOutstandingFine(10.0);
                return u;
            }
        };

        adminService.unregisterUser("Debtor", stubUserSrv, null);
        assertTrue(outContent.toString().contains("Cannot delete user! They have unpaid fines"));
    }

    @Test
    @DisplayName("Unregister: Fail (User Has Borrowed Books)")
    void testUnregisterUserHasBooks() {
        adminService.loginAdmin(adminUser);
        
        UserService stubUserSrv = new UserService("dummy_users.txt") {
            @Override
            public User findUserByName(String name) {
                return new User("Borrower", "pass", "User"); // غرامة 0
            }
        };

        // Stubbing BookService
        BookService stubBookSrv = new BookService(null, null, "dummy.txt") {
            @Override
            public List<media> getAllBooks() {
                Book b = new Book("B1", "A1", "111");
                b.setBorrowed(true);
                b.setBorrowedBy(new User("Borrower", "", "User"));
                List<media> l = new ArrayList<>();
                l.add(b);
                return l;
            }
        };

        adminService.unregisterUser("Borrower", stubUserSrv, stubBookSrv);
        assertTrue(outContent.toString().contains("Cannot delete user! They still have borrowed books"));
    }

    @Test
    @DisplayName("Unregister: Success")
    void testUnregisterSuccess() {
        adminService.loginAdmin(adminUser);
        
        UserService stubUserSrv = new UserService("dummy_users.txt") {
            @Override
            public User findUserByName(String name) {
                return new User("CleanUser", "pass", "User");
            }
            @Override
            public boolean deleteUser(String name) {
                return true;
            }
        };

        BookService stubBookSrv = new BookService(null, null, "dummy.txt") {
            @Override
            public List<media> getAllBooks() {
                return Collections.emptyList();
            }
        };

        adminService.unregisterUser("CleanUser", stubUserSrv, stubBookSrv);
        assertTrue(outContent.toString().contains("unregistered successfully"));
    }

    // =================================================================
    // 4. Test sendOverdueReminders
    // =================================================================

    @Test
    @DisplayName("Send Reminders: Fail (Not Logged In)")
    void testSendRemindersNotLoggedIn() {
        adminService.sendOverdueReminders(null, null);
        assertTrue(outContent.toString().contains("Access denied"));
    }

    @Test
    @DisplayName("Send Reminders: Success (Triggered)")
    void testSendRemindersSuccess() {
        adminService.loginAdmin(adminUser);
        
        // نحن فقط نتأكد أن الدالة تم استدعاؤها ولم تنهار
        UserService stubUserSrv = new UserService("dummy_users.txt");
        
        BookService stubBookSrv = new BookService(null, null, "dummy.txt") {
            @Override
            public List<media> getAllBooks() { return new ArrayList<>(); }
        };

        adminService.sendOverdueReminders(stubUserSrv, stubBookSrv);
        assertTrue(outContent.toString().contains("Initiating notification process"));
    }
}