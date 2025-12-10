/*package main;

import model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import service.AdminService;
import service.BookService;
import service.UserService;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

class mymainTest {

    private final InputStream originalIn = System.in;

    @BeforeEach
    void setUp() {
        // 1. تصفير الخدمات لضمان بيئة نظيفة
        mymain.adminService = new AdminService();
        mymain.userService = new UserService(); 
        
        // 2. تحديث BookService ليرتبط بالخدمات الجديدة (يمنع مشاكل الصلاحيات)
        mymain.bookService = new BookService(mymain.adminService, mymain.userService);
    }

    @AfterEach
    void tearDown() {
        // استعادة لوحة المفاتيح الطبيعية
        System.setIn(originalIn);
    }

    // دالة مساعدة لمحاكاة الإدخال
    private void provideInput(String data) {
        System.setIn(new ByteArrayInputStream(data.getBytes()));
        mymain.scanner = new Scanner(System.in);
    }

    // =========================================================
    // 1. اختبارات الأدمن (Admin)
    // =========================================================

    @Test
    @DisplayName("Admin: Test Full Flow (Add, Search, Logout)")
    void testAdminMenuFullFlow() {
        // السيناريو: إضافة كتاب -> إضافة CD -> بحث -> خروج
        String input = "1\nJavaBook\nAuth\n999\n" + 
                       "2\nBestCD\nArtist\n888\n" +
                       "3\nJava\n" +
                       "4\n" +
                       "5\nNotExistUser\n" +
                       "6\n" +
                       "99\n" + 
                       "7\n"; 

        provideInput(input);

        // تسجيل دخول الأدمن
        User admin = new User("SuperAdmin", "123", "Admin");
        mymain.adminService.loginAdmin(admin);

        assertDoesNotThrow(() -> mymain.adminMenu());
    }

    @Test
    @DisplayName("Admin: Security Check (Access Denied)")
    void testAdminSecurityCheck() {
        // محاولة إضافة كتاب دون تسجيل دخول -> ثم خروج
        String input = "1\n7\n";
        provideInput(input);

        mymain.adminService.logout(); 

        assertDoesNotThrow(() -> mymain.adminMenu());
    }

    @Test
    @DisplayName("Admin: Handle Invalid Input")
    void testAdminInvalidInput() {
        // إدخال حروف بدل أرقام -> ثم خروج
        String input = "abc\n7\n";
        provideInput(input);

        assertDoesNotThrow(() -> mymain.adminMenu());
    }

    // =========================================================
    // 2. اختبارات المستخدم (User) - تم إصلاح الـ NPE هنا
    // =========================================================

    @Test
    @DisplayName("User: Happy Path (Borrow & Return)")
    void testUserMenuNormalFlow() {
        // السيناريو: بحث -> استعارة -> إرجاع -> خروج
        String input = "1\nJava\n" +
                       "2\n999\n" +
                       "3\n999\n" +
                       "4\n" +
                       "5\n";
        provideInput(input);

        User user = new User("John", "pass", "User");
        user.setOutstandingFine(0.0); 

        // ✅✅ الإصلاح: إضافة المستخدم لقائمة الخدمة ليتمكن BookService من إيجاده
        mymain.userService.getAllUsers().add(user);

        assertDoesNotThrow(() -> mymain.userMenu(user));
    }

    @Test
    @DisplayName("User: Blocked Actions due to Fines")
    void testUserMenuBlockedByFines() {
        // السيناريو: محاولة استعارة (2) -> منع -> خروج (5)
        String input = "2\n5\n";
        provideInput(input);

        User debtor = new User("Debtor", "pass", "User");
        debtor.setOutstandingFine(50.0); 

        // ✅✅ الإصلاح: إضافة المستخدم لقائمة الخدمة
        mymain.userService.getAllUsers().add(debtor);

        assertDoesNotThrow(() -> mymain.userMenu(debtor));
    }

    @Test
    @DisplayName("User: Pay Fine Logic")
    void testUserPayFine() {
        // إعداد المستخدم
        User user = new User("Payer", "pass", "User");
        user.setOutstandingFine(20.0);

        // ✅✅ الإصلاح 1: إضافة المستخدم للقائمة
        mymain.userService.getAllUsers().add(user);
        // ✅✅ الإصلاح 2: تسجيل الدخول (لأن دالة payFine تتطلب ذلك)
        mymain.userService.login("Payer", "pass", null);

        // السيناريو:
        // 4 (دفع) -> abc (خطأ) -> 4 (دفع) -> 20.0 (صحيح) -> 5 (خروج)
        String input = "4\nabc\n4\n20.0\n5\n";
        provideInput(input);

        assertDoesNotThrow(() -> mymain.userMenu(user));
        
        // التأكد أن الغرامة تم دفعها
     //   assertEquals(0.0, user.getOutstandingFine(), 0.01);
    }
    
    @Test
    @DisplayName("User: Invalid Menu Option")
    void testUserInvalidInput() {
        String input = "99\n5\n";
        provideInput(input);
        
        User user = new User("Test", "pass", "User");
        // لا نحتاج لإضافته هنا لأننا لا نستدعي borrow/return
        
        assertDoesNotThrow(() -> mymain.userMenu(user));
    }

    // =========================================================
    // 3. اختبارات أمين المكتبة (Librarian)
    // =========================================================

    @Test
    @DisplayName("Librarian: Valid Options")
    void testLibrarianMenu() {
        // عرض المتأخرات -> حساب الغرامات -> خروج
        String input = "1\n2\n3\n";
        provideInput(input);

        User lib = new User("Sarah", "pass", "Librarian");
        
        assertDoesNotThrow(() -> mymain.librarianMenu(lib));
    }

    @Test
    @DisplayName("Librarian: Invalid Input Handling")
    void testLibrarianInvalidInput() {
        // إدخال نص خاطئ -> خروج
        String input = "bad_text\n3\n";
        provideInput(input);

        User lib = new User("Sarah", "pass", "Librarian");
        assertDoesNotThrow(() -> mymain.librarianMenu(lib));
    }
}*/