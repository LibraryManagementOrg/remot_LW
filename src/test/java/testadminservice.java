import service.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import model.Book; // نفترض أن Book يرث من media
import model.User;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private UserService userServiceMock;

    @Mock
    private BookService bookServiceMock;

    private AdminService adminService;
    private User adminUser;

    @BeforeEach
    void setUp() {
        adminService = new AdminService();
        // تجهيز يوزر أدمن للاستخدام في الاختبارات
        adminUser = new User("SuperAdmin", "1234", "Admin", 0.0, "admin@test.com");
    }

    // ========================================================
    // 1️⃣ اختبار تسجيل الدخول والخروج (Login / Logout)
    // ========================================================
    @Test
    void testLoginAdmin_Success() {
        adminService.loginAdmin(adminUser);
        assertTrue(adminService.isLoggedIn(), "Admin should be logged in");
        assertEquals(adminUser, adminService.getCurrentUser());
    }

    @Test
    void testLoginAdmin_Fail_IfUserIsNotAdmin() {
        User regularUser = new User("Normal", "123", "User");
        adminService.loginAdmin(regularUser);
        
        assertFalse(adminService.isLoggedIn(), "Should not login if role is not Admin");
        assertNull(adminService.getCurrentUser());
    }

    @Test
    void testLogout() {
        adminService.loginAdmin(adminUser);
        adminService.logout();
        assertFalse(adminService.isLoggedIn());
        assertNull(adminService.getCurrentUser());
    }

    // ========================================================
    // 2️⃣ اختبار حذف المستخدم (Unregister User) - الحالات المختلفة
    // ========================================================

    @Test
    void testUnregisterUser_Fail_IfNotLoggedIn() {
        // لم نسجل دخول
        adminService.unregisterUser("SomeUser", userServiceMock, bookServiceMock);

        // نتأكد أنه لم يتم استدعاء دالة الحذف
        verify(userServiceMock, never()).deleteUser(anyString());
    }

    @Test
    void testUnregisterUser_Fail_IfUserHasFines() {
        // 1. تسجيل الدخول
        adminService.loginAdmin(adminUser);

        // 2. تجهيز يوزر عليه غرامات
        User debtUser = new User("Debtor", "123", "User", 50.0, "debt@test.com");
        
        // 3. برمجة الموك
        when(userServiceMock.findUserByName("Debtor")).thenReturn(debtUser);

        // 4. تنفيذ
        adminService.unregisterUser("Debtor", userServiceMock, bookServiceMock);

        // 5. تحقق: يجب ألا يتم الحذف
        verify(userServiceMock, never()).deleteUser("Debtor");
    }

    @Test
    void testUnregisterUser_Fail_IfUserHasBorrowedBooks() {
        // 1. تسجيل الدخول
        adminService.loginAdmin(adminUser);

        // 2. تجهيز يوزر نظيف من الغرامات
        User activeUser = new User("Active", "123", "User", 0.0, "active@test.com");

        // 3. تجهيز كتاب مستعار من قبل هذا اليوزر
        Book borrowedBook = new Book("Java", "Author", "123");
        borrowedBook.setBorrowed(true);
        borrowedBook.setBorrowedBy(activeUser); // الكتاب مع هذا اليوزر

        // 4. برمجة الموك
        when(userServiceMock.findUserByName("Active")).thenReturn(activeUser);
        when(bookServiceMock.getAllBooks()).thenReturn(Arrays.asList(borrowedBook));

        // 5. تنفيذ
        adminService.unregisterUser("Active", userServiceMock, bookServiceMock);

        // 6. تحقق: يجب ألا يتم الحذف لأن لديه كتاب
        verify(userServiceMock, never()).deleteUser("Active");
    }

    @Test
    void testUnregisterUser_Success() {
        // 1. تسجيل الدخول
        adminService.loginAdmin(adminUser);

        // 2. تجهيز يوزر جاهز للحذف (لا غرامات ولا كتب)
        User targetUser = new User("Target", "123", "User", 0.0, "target@test.com");

        // 3. برمجة الموك
        when(userServiceMock.findUserByName("Target")).thenReturn(targetUser);
        // قائمة الكتب فارغة أو كتب غير مستعارة من قبله
        when(bookServiceMock.getAllBooks()).thenReturn(Collections.emptyList());
        when(userServiceMock.deleteUser("Target")).thenReturn(true);

        // 4. تنفيذ
        adminService.unregisterUser("Target", userServiceMock, bookServiceMock);

        // 5. تحقق: دالة الحذف تم استدعاؤها
        verify(userServiceMock, times(1)).deleteUser("Target");
    }

    // ========================================================
    // 3️⃣ اختبار الوصول لدالة الإيميلات (Send Reminders)
    // ========================================================
    @Test
    void testSendReminders_AccessControl() {
        // محاولة الإرسال بدون تسجيل دخول
        adminService.sendOverdueReminders(userServiceMock, bookServiceMock);
        
        // بما أننا لم نسجل دخول، الكود يطبع خطأ ويخرج
        // هنا فقط نتأكد أن الكود لا ينهار (Exception Free)
        assertFalse(adminService.isLoggedIn());
    }
}