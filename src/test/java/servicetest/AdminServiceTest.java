package servicetest;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import model.Book;
import model.User;
import model.media;
import service.AdminService;
import service.BookService;
import service.UserService;

class AdminServiceTest {

    private AdminService adminService;
    private User adminUser;
    private User regularUser;
    
    // لالتقاط الرسائل التي تطبع في Console
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    void setUp() {
        adminService = new AdminService();
        adminUser = new User("SuperAdmin", "123", "Admin");
        regularUser = new User("JohnDoe", "pass", "User");
        
        // تحويل مسار الطباعة لالتقاط الرسائل وفحصها
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    void tearDown() {
        // إرجاع مسار الطباعة للوضع الطبيعي بعد كل اختبار
        System.setOut(originalOut);
    }

    // =================================================================
    // 1. اختبار تسجيل الدخول والخروج (Login & Logout)
    // =================================================================
    
    @Test
    @DisplayName("Login: Success with Admin User")
    void testLoginSuccess() {
        adminService.loginAdmin(adminUser);
        
        assertTrue(adminService.isLoggedIn(), "Should be logged in");
        assertEquals(adminUser, adminService.getCurrentUser());
        assertTrue(outContent.toString().contains("Admin session started"));
    }

    @Test
    @DisplayName("Login: Fail with Regular User")
    void testLoginFailRegularUser() {
        adminService.loginAdmin(regularUser);
        
        assertFalse(adminService.isLoggedIn(), "Should not be logged in");
        assertTrue(outContent.toString().contains("Access denied"));
    }

    @Test
    @DisplayName("Login: Fail with Null User")
    void testLoginFailNull() {
        adminService.loginAdmin(null);
        
        assertFalse(adminService.isLoggedIn());
        assertTrue(outContent.toString().contains("Access denied")); // يغطي الـ else
    }

    @Test
    @DisplayName("Logout: Should reset state")
    void testLogout() {
        // ترتيب: تسجيل دخول أولاً
        adminService.loginAdmin(adminUser);
        
        // فعل: تسجيل خروج
        adminService.logout();
        
        // تحقق
        assertFalse(adminService.isLoggedIn());
        assertNull(adminService.getCurrentUser());
        assertTrue(outContent.toString().contains("logged out successfully"));
    }

    // =================================================================
    // 2. اختبار عرض الكتب (Show All Books)
    // =================================================================

    @Test
    @DisplayName("Show Books: Fail when Not Logged In")
    void testShowBooksNotLoggedIn() {
        adminService.showAllBooks(null);
        assertTrue(outContent.toString().contains("Access denied"));
    }

    @Test
    @DisplayName("Show Books: Empty List")
    void testShowBooksEmpty() {
        adminService.loginAdmin(adminUser);
        
        // إنشاء BookService وهمي يرجع قائمة فارغة
        // نمرر null للكونستركتور لأننا سنعيد تعريف الدالة getAllBooks فقط
        BookService stubBookService = new BookService(null, null, "dummy.txt") {
            @Override
            public List<media> getAllBooks() {
                return Collections.emptyList();
            }
        };

        adminService.showAllBooks(stubBookService);
        assertTrue(outContent.toString().contains("No books available"));
    }

    @Test
    @DisplayName("Show Books: List with Items")
    void testShowBooksWithItems() {
        adminService.loginAdmin(adminUser);
        
        // إنشاء BookService وهمي يرجع كتاب واحد
        BookService stubBookService = new BookService(null, null, "dummy.txt") {
            @Override
            public List<media> getAllBooks() {
                List<media> list = new ArrayList<>();
                list.add(new Book("Java Programming", "Author", "123"));
                return list;
            }
        };

        adminService.showAllBooks(stubBookService);
        assertTrue(outContent.toString().contains("All Books:"));
        assertTrue(outContent.toString().contains("Java Programming"));
    }

    // =================================================================
    // 3. اختبار حذف المستخدم (Unregister User) - أهم جزء للكفرج
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
        
        // UserService وهمي يرجع null عند البحث
        UserService stubUserService = new UserService("dummy.txt") {
            @Override
            public User findUserByName(String name) {
                return null; 
            }
        };

        adminService.unregisterUser("GhostUser", stubUserService, null);
        assertTrue(outContent.toString().contains("User not found"));
    }

    @Test
    @DisplayName("Unregister: Fail (User Has Unpaid Fines)")
    void testUnregisterUserHasFines() {
        adminService.loginAdmin(adminUser);
        
        // UserService وهمي يرجع مستخدم عليه غرامات
        UserService stubUserService = new UserService("dummy.txt") {
            @Override
            public User findUserByName(String name) {
                User u = new User("BadUser", "pass", "User");
                u.setOutstandingFine(50.0); // عليه غرامة
                return u;
            }
        };

        adminService.unregisterUser("BadUser", stubUserService, null);
        assertTrue(outContent.toString().contains("Cannot delete user! They have unpaid fines"));
    }

    @Test
    @DisplayName("Unregister: Fail (User Has Borrowed Books)")
    void testUnregisterUserHasBooks() {
        adminService.loginAdmin(adminUser);
        String targetUser = "Reader";
        
        // UserService يرجع مستخدم سليم (بدون غرامات)
        UserService stubUserService = new UserService("dummy.txt") {
            @Override
            public User findUserByName(String name) {
                return new User(targetUser, "pass", "User");
            }
        };

        // BookService يرجع كتاباً مستعاراً من قبل هذا المستخدم
        BookService stubBookService = new BookService(null, null, "dummy.txt") {
            @Override
            public List<media> getAllBooks() {
                Book b = new Book("Book1", "Auth", "111");
                User u = new User(targetUser, "pass", "User");
                b.borrow(u); // نجعل الكتاب مستعاراً
                
                List<media> list = new ArrayList<>();
                list.add(b);
                return list;
            }
        };

        adminService.unregisterUser(targetUser, stubUserService, stubBookService);
        assertTrue(outContent.toString().contains("Cannot delete user! They still have borrowed books"));
    }

    @Test
    @DisplayName("Unregister: Success")
    void testUnregisterSuccess() {
        adminService.loginAdmin(adminUser);
        String targetUser = "GoodUser";

        // UserService جاهز للحذف
        UserService stubUserService = new UserService("dummy.txt") {
            @Override
            public User findUserByName(String name) {
                return new User(targetUser, "pass", "User");
            }
            @Override
            public boolean deleteUser(String name) {
                return true; // محاكاة الحذف الناجح
            }
        };

        // BookService يرجع قائمة خالية (أو كتب غير مستعارة من هذا المستخدم)
        BookService stubBookService = new BookService(null, null, "dummy.txt") {
            @Override
            public List<media> getAllBooks() {
                return Collections.emptyList();
            }
        };

        adminService.unregisterUser(targetUser, stubUserService, stubBookService);
        assertTrue(outContent.toString().contains("unregistered successfully"));
    }

    // =================================================================
    // 4. اختبار التنبيهات (Reminders)
    // =================================================================

    @Test
    @DisplayName("Send Reminders: Fail (Not Logged In)")
    void testSendRemindersNotLoggedIn() {
        adminService.sendOverdueReminders(null, null);
        assertTrue(outContent.toString().contains("Access denied"));
    }

    @Test
    @DisplayName("Send Reminders: Success Flow")
    void testSendRemindersSuccess() {
        adminService.loginAdmin(adminUser);
        
        // نمرر خدمات وهمية بسيطة لتجنب أخطاء الملفات
        UserService stubUserService = new UserService("dummy.txt");
        BookService stubBookService = new BookService(null, null, "dummy.txt") {
            @Override
            public List<media> getAllBooks() {
                return new ArrayList<>(); // قائمة فارغة لتجنب تعقيد الإيميلات
            }
        };

        // الهدف هنا التأكد من أن الدالة تعمل ولا تنهار، وأنها تطبع رسالة البدء
        adminService.sendOverdueReminders(stubUserService, stubBookService);
        assertTrue(outContent.toString().contains("Initiating notification process"));
    }
}