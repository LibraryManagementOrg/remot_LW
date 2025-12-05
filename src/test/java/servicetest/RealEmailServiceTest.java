package servicetest;

import model.User;
import service.RealEmailService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RealEmailServiceTest {

    private RealEmailService emailService;
    private User testUser;

    @BeforeEach
    void setUp() {
        emailService = new RealEmailService();
        // نستخدم إيميل وهمي لضمان عدم إزعاج أي شخص حقيقي أثناء الاختبار
        testUser = new User("Test Student", "123", "User");
        testUser.setEmail("fake_email_for_testing@invalid-domain.com");
    }

    @Test
    @DisplayName("1. Test Email Sending (Exception Handling)")
    void testUpdateDoesNotCrash() {
        // الهدف: التأكد من أن الكود يحتوي على Try-Catch
        // وأنه لا يوقف البرنامج إذا فشل الاتصال بجيميل أو كان الإيميل خطأ.
        
        // عند تشغيل هذا السطر، سيحاول الاتصال، وسيفشل غالباً، ويطبع رسالة الخطأ في الكونسول
        // ولكن الاختبار سينجح إذا لم يحدث Crash.
        assertDoesNotThrow(() -> {
            emailService.update(testUser, "This is a test message form JUnit.");
        }, "The service should catch MessagingException and not crash the app");
    }

    @Test
    @DisplayName("2. Test with Null Email (Edge Case)")
    void testUpdateWithNullEmail() {
        // تجهيز مستخدم بدون إيميل (null)
        User userNoEmail = new User("NoEmailUser", "123", "User"); 
        userNoEmail.setEmail(null); // تأكد

        // عند محاولة الإرسال لـ null، مكتبة JavaMail سترمي Exception
        // نتأكد أن الكلاس الخاص بك يصطاد هذا الخطأ ولا ينهار
        assertDoesNotThrow(() -> {
            emailService.update(userNoEmail, "Test Message");
        });
    }
}