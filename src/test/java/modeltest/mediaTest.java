package modeltest;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import model.User;
import model.media;
import org.junit.jupiter.api.DisplayName;
import java.time.LocalDate;

class mediaTest {

    // متغير لتخزين الكائن الذي سنختبره
    private media testMedia;
    private User user;

    @BeforeEach
    void setUp() {
        user = new User("TestUser", "pass", "User");

        // 🌟 إنشاء كائن من Media باستخدام (Anonymous Class)
        // هذا يسمح لنا باختبار الكلاس المجرد دون الحاجة لـ Book أو CD
        testMedia = new media("Generic Title", "Generic Creator", "000-000") {
            @Override
            public int getLoanPeriod() {
                return 10; // قيمة افتراضية للاختبار
            }

            @Override
            public double getDailyFine() {
                return 5.0; // قيمة افتراضية للاختبار
            }
        };
    }

    @Test
    @DisplayName("1. Test Constructor and Getters")
    void testConstructor() {
        assertEquals("Generic Title", testMedia.getTitle());
        assertEquals("Generic Creator", testMedia.getCreator());
        assertEquals("000-000", testMedia.getId());
        
        // القيم الافتراضية
        assertFalse(testMedia.isBorrowed());
        assertNull(testMedia.getDueDate());
        assertFalse(testMedia.isFineIssued());
    }

    @Test
    @DisplayName("2. Test Setters")
    void testSetters() {
        testMedia.setBorrowed(true);
        testMedia.setBorrowedBy(user);
        LocalDate date = LocalDate.now().plusDays(5);
        testMedia.setDueDate(date);
        testMedia.setFineIssued(true);

        assertTrue(testMedia.isBorrowed());
        assertEquals(user, testMedia.getBorrowedBy());
        assertEquals(date, testMedia.getDueDate());
        assertTrue(testMedia.isFineIssued());
    }

    @Test
    @DisplayName("3. Test isOverdue Logic")
    void testIsOverdue() {
        // الحالة 1: غير مستعار -> False
        testMedia.setBorrowed(false);
        assertFalse(testMedia.isOverdue(), "Should not be overdue if not borrowed");

        // الحالة 2: مستعار لكن التاريخ في المستقبل -> False
        testMedia.setBorrowed(true);
        testMedia.setDueDate(LocalDate.now().plusDays(1));
        assertFalse(testMedia.isOverdue(), "Should not be overdue if due date is in future");

        // الحالة 3: مستعار والتاريخ اليوم -> False
        testMedia.setDueDate(LocalDate.now());
        assertFalse(testMedia.isOverdue(), "Should not be overdue if due date is today");

        // الحالة 4: مستعار والتاريخ في الماضي -> True
        testMedia.setDueDate(LocalDate.now().minusDays(1));
        assertTrue(testMedia.isOverdue(), "Should be overdue if due date is in past");
    }

    @Test
    @DisplayName("4. Test Fine Calculation (Default fallback to getDailyFine)")
    void testFineAmountWithoutStrategy() {
        // سيناريو: تأخير يومين، بدون استراتيجية
        // يجب أن يستخدم getDailyFine() التي عرفناها بـ 5.0
        
        testMedia.setBorrowed(true);
        // تأخير يومين (تاريخ الاستحقاق قبل يومين)
        testMedia.setDueDate(LocalDate.now().minusDays(2));

        // تأكد أنه متأخر أولاً
        assertTrue(testMedia.isOverdue());

        // الحساب المتوقع: 2 يوم * 5.0 = 10.0
        assertEquals(10.0, testMedia.getFineAmount(), 0.01);
    }

    @Test
    @DisplayName("5. Test Fine Calculation (With Strategy)")
    void testFineAmountWithStrategy() {
        // سيناريو: نضع استراتيجية مخصصة تضاعف الغرامة مثلاً
        // Strategy: days * 100
        testMedia.setFineStrategy(days -> days * 100.0);

        testMedia.setBorrowed(true);
        // تأخير 3 أيام
        testMedia.setDueDate(LocalDate.now().minusDays(3));

        // الحساب المتوقع: 3 أيام * 100.0 (من الاستراتيجية) = 300.0
        // (ويجب أن يتجاهل الـ Default 5.0)
        assertEquals(300.0, testMedia.getFineAmount(), 0.01);
    }
    
    @Test
    @DisplayName("6. Test Fine is Zero if Not Overdue")
    void testFineZeroIfNotOverdue() {
        testMedia.setBorrowed(true);
        testMedia.setDueDate(LocalDate.now().plusDays(5)); // المستقبل
        
        assertEquals(0.0, testMedia.getFineAmount());
    }

    @Test
    @DisplayName("7. Test toString")
    void testToString() {
        String output = testMedia.toString();
        // بما أننا استخدمنا anonymous subclass، اسم الكلاس سيكون فارغاً أو غريباً قليلاً
        // لذا نتحقق من بقية النص
        assertTrue(output.contains("Generic Title"));
        assertTrue(output.contains("Generic Creator"));
        assertTrue(output.contains("000-000"));
    }
}
