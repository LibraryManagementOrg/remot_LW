package main;
import main.mymain;
import model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class mymainTest {

    private final InputStream originalIn = System.in;

    @BeforeEach
    void setUp() {
        // تهيئة الخدمات المشتركة في mymain (إذا كانت public static، يمكن الوصول إليها)
        // ولكن بما أننا نختبر الدوال static void، فالأهم هو التحكم بالـ Scanner
    }

    @AfterEach
    void tearDown() {
        // استعادة System.in الأصلي
        System.setIn(originalIn);
    }

    @Test
    @DisplayName("1. Test Admin Menu Flow (Add Book -> Logout)")
    void testAdminMenuFlow() {
        // محاكاة إدخال المستخدم:
        // 1 (Add Book) -> Title -> Author -> ISBN
        // 7 (Logout)
        String simulatedInput = "1\nTestBook\nTestAuthor\n999-999\n7\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));

        // إعادة تعيين الـ Scanner ليقرأ من المدخل الجديد
        mymain.scanner = new Scanner(System.in);

        // يجب تسجيل الدخول كأدمن أولاً في الخدمة لكي تعمل الخيارات
        User admin = new User("SuperAdmin", "123", "Admin");
        mymain.adminService.loginAdmin(admin);

        // تشغيل القائمة
        assertDoesNotThrow(() -> mymain.adminMenu());
    }

    @Test
    @DisplayName("2. Test User Menu Flow (Search -> Logout)")
    void testUserMenuFlow() {
        // محاكاة إدخال المستخدم:
        // 1 (Search) -> keyword "java"
        // 5 (Logout)
        String simulatedInput = "1\njava\n5\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));

        mymain.scanner = new Scanner(System.in);

        User user = new User("John", "123", "User");
        
        // تشغيل القائمة
        assertDoesNotThrow(() -> mymain.userMenu(user));
    }

    @Test
    @DisplayName("3. Test Librarian Menu Flow (Show Overdue -> Logout)")
    void testLibrarianMenuFlow() {
        // محاكاة إدخال المستخدم:
        // 1 (Show Overdue)
        // 3 (Logout)
        String simulatedInput = "1\n3\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));

        mymain.scanner = new Scanner(System.in);

        User librarian = new User("Sarah", "123", "Librarian");

        // تشغيل القائمة
        assertDoesNotThrow(() -> mymain.librarianMenu(librarian));
    }
}