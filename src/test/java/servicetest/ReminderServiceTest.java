package servicetest;

import model.Book;
import model.CD;
import model.User;
import model.media;
import service.NotificationObserver;
import service.ReminderService;
import service.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReminderServiceTest {

    private ReminderService reminderService;
    private UserService stubUserService;
    
    // متغيرات لتتبع نتائج الـ Observer
    private boolean observerCalled;
    private String capturedMessage;
    private User capturedUser;

    // مستخدمون للاختبار
    private User validUser;
    private User adminUser;
    private User noEmailUser;

    @BeforeEach
    void setUp() {
        // تهيئة المتغيرات
        observerCalled = false;
        capturedMessage = "";
        capturedUser = null;

        // 1. تعريف المستخدمين
        validUser = new User("Alice", "123", "User");
        validUser.setEmail("alice@test.com");

        adminUser = new User("Admin", "123", "Admin");
        adminUser.setEmail("admin@test.com");

        noEmailUser = new User("Bob", "123", "User");
        noEmailUser.setEmail(null); // لا يملك إيميل

        // 2. Stubbing UserService (إرجاع قائمة ثابتة)
        stubUserService = new UserService() {
            @Override
            public List<User> getAllUsers() {
                List<User> users = new ArrayList<>();
                users.add(validUser);
                users.add(adminUser);
                users.add(noEmailUser);
                return users;
            }
        };

        // 3. Mocking Observer (لالتقاط الرسالة بدلاً من إرسالها)
        NotificationObserver mockObserver = new NotificationObserver() {
            @Override
            public void update(User user, String message) {
                observerCalled = true;
                capturedUser = user;
                capturedMessage = message;
            }
        };

        // 4. إنشاء الخدمة
        reminderService = new ReminderService(mockObserver, stubUserService);
    }

    @Test
    @DisplayName("1. Test Success: Send Email to Valid User with Overdue Items")
    void testSendOverdueReminders_Success() {
        // تجهيز قائمة الكتب
        List<media> items = new ArrayList<>();

        // كتاب متأخر (يجب أن يظهر في الإيميل)
        Book overdueBook = new Book("Late Java", "Author", "1");
        overdueBook.setBorrowed(true);
        overdueBook.setBorrowedBy(validUser);
        overdueBook.setDueDate(LocalDate.now().minusDays(5)); // متأخر 5 أيام
        items.add(overdueBook);

        // CD متأخر (يجب أن يظهر في الإيميل)
        CD overdueCD = new CD("Late Audio", "Artist", "2");
        overdueCD.setBorrowed(true);
        overdueCD.setBorrowedBy(validUser);
        overdueCD.setDueDate(LocalDate.now().minusDays(2)); // متأخر يومين
        items.add(overdueCD);

        // كتاب غير متأخر (لا يجب أن يظهر)
        Book onTimeBook = new Book("Good Book", "Author", "3");
        onTimeBook.setBorrowed(true);
        onTimeBook.setBorrowedBy(validUser);
        onTimeBook.setDueDate(LocalDate.now().plusDays(5));
        items.add(onTimeBook);

        // التنفيذ
        reminderService.sendOverdueReminders(items);

        // التحقق
        assertTrue(observerCalled, "Observer should be called for valid user");
        assertEquals(validUser, capturedUser);
        
        // التحقق من محتوى الرسالة
        assertTrue(capturedMessage.contains("Late Java"), "Message should contain Book title");
        assertTrue(capturedMessage.contains("[Book]"), "Message should contain type [Book]");
        assertTrue(capturedMessage.contains("Late Audio"), "Message should contain CD title");
        assertTrue(capturedMessage.contains("[CD]"), "Message should contain type [CD]");
        
        // التأكد من أن الكتاب غير المتأخر لم يذكر
        assertFalse(capturedMessage.contains("Good Book"), "On-time items should not be in the email");
    }

    @Test
    @DisplayName("2. Test Ignore: Admin User")
    void testIgnoreAdmin() {
        List<media> items = new ArrayList<>();
        
        // كتاب متأخر للأدمن
        Book adminBook = new Book("Admin Book", "Auth", "10");
        adminBook.setBorrowed(true);
        adminBook.setBorrowedBy(adminUser);
        adminBook.setDueDate(LocalDate.now().minusDays(10));
        items.add(adminBook);

        reminderService.sendOverdueReminders(items);

        assertFalse(observerCalled, "Should NOT send email to Admin even if items are overdue");
    }

    @Test
    @DisplayName("3. Test Ignore: User without Email")
    void testIgnoreNoEmail() {
        List<media> items = new ArrayList<>();

        // كتاب متأخر لمستخدم بلا إيميل
        Book noEmailBook = new Book("Ghost Book", "Auth", "20");
        noEmailBook.setBorrowed(true);
        noEmailBook.setBorrowedBy(noEmailUser);
        noEmailBook.setDueDate(LocalDate.now().minusDays(10));
        items.add(noEmailBook);

        reminderService.sendOverdueReminders(items);

        assertFalse(observerCalled, "Should NOT send email if user has no email address");
    }

    @Test
    @DisplayName("4. Test Ignore: No Overdue Items")
    void testNoOverdueItems() {
        List<media> items = new ArrayList<>();

        // كتاب مستعار ولكنه غير متأخر
        Book safeBook = new Book("Safe Book", "Auth", "30");
        safeBook.setBorrowed(true);
        safeBook.setBorrowedBy(validUser);
        safeBook.setDueDate(LocalDate.now().plusDays(1)); // غداً
        items.add(safeBook);

        reminderService.sendOverdueReminders(items);

        assertFalse(observerCalled, "Should NOT send email if no items are overdue");
    }
}