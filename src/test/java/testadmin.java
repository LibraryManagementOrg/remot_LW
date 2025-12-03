import model.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class AdminTest {

    @Test
    void testAdminCreationAndGetters() {
        // 1. تجهيز البيانات (Arrange)
        String expectedUsername = "super_admin";
        String expectedPassword = "securePassword123";

        // 2. تنفيذ الفعل - إنشاء الكائن (Act)
        Admin admin = new Admin(expectedUsername, expectedPassword);

        // 3. التحقق من النتائج (Assert)
        
        // التأكد أن الكائن ليس null
        assertNotNull(admin, "Admin object should be created");
        
        // التأكد أن القيم المخزنة هي نفسها التي أرسلناها
        assertEquals(expectedUsername, admin.getUsername(), "Username should match the constructor input");
        assertEquals(expectedPassword, admin.getPassword(), "Password should match the constructor input");
    }

    @Test
    void testAdminWithNullValues() {
        // اختبار حالة الحواف: إرسال قيم null (للتأكد أن الكود لا ينهار)
        Admin admin = new Admin(null, null);

        assertNull(admin.getUsername());
        assertNull(admin.getPassword());
    }
}