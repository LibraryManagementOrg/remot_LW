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
    private final String TEST_FILE_PATH = "src/main/resources/users_login_test.txt";
    private File userFile;
    private final InputStream originalIn = System.in;

    @BeforeEach
    void setUp() throws IOException {
        // إنشاء المجلد إذا لم يكن موجوداً
        File directory = new File("src/main/resources");
        if (!directory.exists()) directory.mkdirs();

        // إنشاء ملف وهمي
        userFile = new File(TEST_FILE_PATH);
        try (FileWriter writer = new FileWriter(userFile)) {
            writer.write("alice,123,User,50.0,alice@test.com\n");
            writer.write("bob,456,Admin\n");
            writer.write("charlie,789,Librarian,10.0\n");
        }
        
        loginService = new LoginService(TEST_FILE_PATH);
    }

    @AfterEach
    void tearDown() {
        System.setIn(originalIn); // استعادة الإدخال الأصلي
        if (userFile.exists()) userFile.delete(); // حذف الملف الوهمي
    }

    @Test
    @DisplayName("1. Direct Login: Full Data (Success)")
    void testLoginFullData() {
        User user = loginService.login("alice", "123");
        assertNotNull(user, "User should be found");
        assertEquals("alice", user.getName());
        assertEquals("User", user.getRole());
        assertEquals(50.0, user.getOutstandingFine());
        assertEquals("alice@test.com", user.getEmail());
    }

    @Test
    @DisplayName("2. Direct Login: Partial Data (Success)")
    void testLoginPartialData() {
        User user = loginService.login("bob", "456"); // Admin with no fine/email in file
        assertNotNull(user);
        assertEquals("bob", user.getName());
        assertEquals("Admin", user.getRole());
        assertEquals(0.0, user.getOutstandingFine()); // Default value
    }

    @Test
    @DisplayName("3. Direct Login: Failures")
    void testLoginFail() {
        assertNull(loginService.login("alice", "wrongpass"), "Wrong password should fail");
        assertNull(loginService.login("ghost", "123"), "Unknown user should fail");
    }

    @Test
    @DisplayName("4. Login Loop: Handling User Input")
    void testLoginLoop() {
        // محاكاة سيناريو:
        // 1. اسم خاطئ -> يعرض رسالة خطأ ويكرر
        // 2. اسم صحيح وكلمة سر خطأ -> يعرض رسالة خطأ ويكرر
        // 3. اسم صحيح وكلمة سر صحيحة -> ينجح ويخرج
        String simulatedInput = "wrong\n123\nalice\nwrongpass\nalice\n123\n";
        
        // توجيه الإدخال
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));

        // ملاحظة هامة: Scanner يتم إنشاؤه داخل الدالة، لذا سيلتقط الإدخال الجديد تلقائياً
        User user = loginService.loginLoop();

        assertNotNull(user, "Should eventually login successfully");
        assertEquals("alice", user.getName());
    }

    @Test
    @DisplayName("5. Robustness: Parsing Bad Lines")
    void testBadLinesInFile() throws IOException {
        // إعادة كتابة الملف ببيانات مشوهة
        try (FileWriter writer = new FileWriter(userFile)) {
            writer.write("\n"); // Empty line
            writer.write("short\n"); // Not enough parts
            writer.write("badUser,pass,User,NotANumber\n"); // Invalid fine
        }
        
        // يجب أن يتجاهل الأسطر السيئة ويقرأ السطر الأخير (مع تصحيح الغرامة إلى 0.0)
        User user = loginService.login("badUser", "pass");
        assertNotNull(user);
        assertEquals(0.0, user.getOutstandingFine());
    }

    @Test
    @DisplayName("6. IO Exception Handling (File Missing)")
    void testFileNotFound() {
        // نحذف الملف لافتعال IOException
        if (userFile.exists()) userFile.delete();

        User result = loginService.login("any", "any");

        assertNotNull(result);
        assertEquals("ERROR", result.getRole());
        assertTrue(result.getName().contains("Cannot read users file"));
    }
    
    @Test
    @DisplayName("7. Login Loop: Error Role Handling")
    void testLoginLoopError() {
        // نحذف الملف لافتعال خطأ القراءة
        if (userFile.exists()) userFile.delete();
        
        // ندخل أي شيء ليحاول الدخول
        String input = "any\nany\n";
        System.setIn(new ByteArrayInputStream(input.getBytes()));
        
        // الدالة يجب أن ترجع null وتطبع رسالة الخطأ عندما يكون الدور ERROR
        User result = loginService.loginLoop();
        assertNull(result);
    }
}