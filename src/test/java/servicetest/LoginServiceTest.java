package servicetest;

import model.User;
import service.LoginService;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;

class LoginServiceTest {

    private LoginService loginService;
    
    // ✅ 1. استخدام اسم ملف مختلف عن الملف الأصلي
    private final String TEST_FILE_PATH = "src/main/resources/users_login_test.txt";
    private File userFile;
    
    // لحفظ الـ System.in الأصلي
    private final InputStream originalIn = System.in;

    @BeforeEach
    void setUp() throws IOException {
        // التأكد من وجود المجلدات
        File directory = new File("src/main/resources");
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // 2. إنشاء ملف وهمي للاختبار
        userFile = new File(TEST_FILE_PATH);
        try (FileWriter writer = new FileWriter(userFile)) {
            // بيانات وهمية
            writer.write("alice,123,User,50.0,alice@test.com\n");
            writer.write("bob,456,Admin\n");
            writer.write("charlie,789,Librarian,10.0\n");
        }
        
        // ✅ 3. تهيئة الخدمة باستخدام الملف الوهمي
        loginService = new LoginService(TEST_FILE_PATH);
    }

    @AfterEach
    void tearDown() {
        // استعادة System.in
        System.setIn(originalIn);

        // 4. حذف الملف الوهمي فقط (الملف الأصلي users.txt بأمان)
        if (userFile.exists()) {
            userFile.delete();
        }
    }

    @Test
    @DisplayName("1. Test Login Direct: Full Data (Success)")
    void testLoginFullData() {
        User user = loginService.login("alice", "123");

        assertNotNull(user);
        assertEquals("alice", user.getName());
        assertEquals("User", user.getRole());
        assertEquals(50.0, user.getOutstandingFine());
        assertEquals("alice@test.com", user.getEmail());
    }

    @Test
    @DisplayName("2. Test Login Direct: Partial Data (Success)")
    void testLoginPartialData() {
        User user = loginService.login("bob", "456");

        assertNotNull(user);
        assertEquals("bob", user.getName());
        assertEquals("Admin", user.getRole());
    }

    @Test
    @DisplayName("3. Test Login Direct: Wrong Credentials (Fail)")
    void testLoginFail() {
        User user = loginService.login("alice", "wrongpass");
        assertNull(user);

        User user2 = loginService.login("notexist", "123");
        assertNull(user2);
    }

    @Test
    @DisplayName("4. Test Login Loop: Simulate User Input")
    void testLoginLoop() {
        // محاكاة المدخلات: خطأ ثم خطأ ثم صحيح
        String simulatedInput = "wrong\n123\nalice\nwrongpass\nalice\n123\n";
        
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));

        // يجب إعادة تهيئة Scanner داخل loginLoop ليقرأ المدخل الجديد
        User user = loginService.loginLoop();

        assertNotNull(user);
        assertEquals("alice", user.getName());
    }

    @Test
    @DisplayName("5. Test File Not Found (IOException Handling)")
    void testFileNotFound() {
        // نحذف الملف الوهمي عمداً
        if (userFile.exists()) {
            userFile.delete();
        }

        User result = loginService.login("any", "any");

        assertNotNull(result);
        assertEquals("ERROR", result.getRole());
        assertTrue(result.getName().contains("Cannot read users file"));
    }
}