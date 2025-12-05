package servicetest;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import model.User;
import service.NotificationObserver;

class NotificationObserverTest {

    @Test
    @DisplayName("Test Observer Contract (Data Transmission)")
    void testUpdateMethod() {
        // 1. التجهيز (Arrange)
        // نستخدم مصفوفات لتخزين القيم التي ستصل داخل الدالة (لأن المتغيرات داخل الكلاس المؤقت يجب أن تكون final أو arrays)
        final User[] capturedUser = new User[1];
        final String[] capturedMessage = new String[1];

        // إنشاء تنفيذ مؤقت للواجهة (Anonymous Class)
        NotificationObserver observer = new NotificationObserver() {
            @Override
            public void update(User user, String message) {
                // نخزن القيم التي وصلت للتأكد منها لاحقاً
                capturedUser[0] = user;
                capturedMessage[0] = message;
            }
        };

        User testUser = new User("TestUser", "123", "User");
        String testMsg = "Hello, you have a fine!";

        // 2. الفعل (Act)
        observer.update(testUser, testMsg);

        // 3. التحقق (Assert)
        // نتأكد أن الدالة تم استدعاؤها وأن البيانات وصلت بشكل صحيح
        assertNotNull(capturedUser[0], "User should be captured");
        assertEquals(testUser, capturedUser[0]);
        assertEquals("Hello, you have a fine!", capturedMessage[0]);
    }

    @Test
    @DisplayName("Test Functional Interface (Lambda)")
    void testLambdaImplementation() {
        // بما أن الواجهة تحتوي دالة واحدة، يمكن استخدامها كـ Lambda
        // هذا اختبار للتأكد من أنها Functional Interface صالحة
        
        NotificationObserver observer = (user, message) -> {
            assertNotNull(user);
            assertTrue(message.length() > 0);
        };

        User u = new User("LambdaUser", "pass", "User");
        
        // تنفيذ الدالة للتأكد من عدم حدوث أخطاء
        assertDoesNotThrow(() -> observer.update(u, "Test Message"));
    }
}