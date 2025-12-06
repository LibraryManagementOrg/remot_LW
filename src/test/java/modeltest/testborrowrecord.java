package modeltest;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import model.Book;
import model.BorrowRecord;
import model.User;

class testborrowrecord {

    private BorrowRecord record;
    // نستخدم null هنا لعدم تعقيد التست، المهم أن نختبر أن الـ Getter يرجع ما تم تمريره
    private Book book = null; 
    private User user = null;

    @BeforeEach
    void setUp() {
        record = new BorrowRecord(book, user);
    }

    @Test
    @DisplayName("1. Test Getters (Coverage for simple returns)")
    void testAllGetters() {
        // هذا الاختبار ضروري لتغطية أسطر: return book; return user;
        assertAll("Verify all getters",
            () -> assertEquals(book, record.getBook()),
            () -> assertEquals(user, record.getUser()),
            () -> assertNotNull(record.getBorrowDate()),
            () -> assertNotNull(record.getDueDate())
        );
    }

    @Test
    @DisplayName("2. Test Constructor Defaults")
    void testConstructorDefaults() {
        assertAll("Constructor Logic",
            () -> assertEquals(LocalDate.now(), record.getBorrowDate()),
            () -> assertEquals(LocalDate.now().plusDays(28), record.getDueDate()),
            () -> assertFalse(record.isReturned())
        );
    }

    @Test
    @DisplayName("3. Test Returned Status")
    void testSetReturned() {
        record.setReturned(true);
        assertTrue(record.isReturned());
        
        record.setReturned(false);
        assertFalse(record.isReturned());
    }

    @Test
    @DisplayName("4. Test isOverdue logic (Time Travel)")
    void testIsOverdueLogic() throws Exception {
        // حالة 1: الوضع الطبيعي (غير متأخر)
        assertFalse(record.isOverdue());

        // حالة 2: تاريخ الاستحقاق هو اليوم (الحد الفاصل - لا يعتبر متأخراً)
        modifyDueDate(record, LocalDate.now());
        assertFalse(record.isOverdue(), "Should not be overdue if due date is today");

        // حالة 3: متأخر بيوم واحد
        modifyDueDate(record, LocalDate.now().minusDays(1));
        assertTrue(record.isOverdue(), "Should be overdue if due date was yesterday");

        // حالة 4: متأخر ولكن تم إرجاعه (يجب أن يكون false)
        record.setReturned(true);
        assertFalse(record.isOverdue(), "Should not be overdue if returned");
    }

    @Test
    @DisplayName("5. Test getDaysOverdue calculations")
    void testDaysOverdue() throws Exception {
        // حالة A: الكتاب غير متأخر
        // هذا يغطي السطر: if (!isOverdue()) return 0;
        assertEquals(0, record.getDaysOverdue());

        // حالة B: الكتاب متأخر
        // نغير التاريخ ليكون قبل 10 أيام
        modifyDueDate(record, LocalDate.now().minusDays(10));
        
        // نتأكد أنه متأخر أولاً
        assertTrue(record.isOverdue());
        // نتأكد من حساب الأيام بشكل صحيح
        assertEquals(10, record.getDaysOverdue());
    }

    @Test
    @DisplayName("6. Test toString")
    void testToString() {
        String result = record.toString();
        assertAll("ToString Content",
            () -> assertTrue(result.contains("borrowDate=")),
            () -> assertTrue(result.contains("dueDate=")),
            () -> assertTrue(result.contains("returned="))
        );
    }

    // ==========================================
    // دالة مساعدة لتعديل التاريخ (Reflection)
    // ==========================================
    private void modifyDueDate(BorrowRecord targetRecord, LocalDate newDate) throws Exception {
        Field field = BorrowRecord.class.getDeclaredField("dueDate");
        field.setAccessible(true);
        field.set(targetRecord, newDate);
    }
}