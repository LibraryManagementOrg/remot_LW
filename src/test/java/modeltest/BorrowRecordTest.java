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

// 🔴 ملاحظة مهمة: تأكدي أن اسم الملف BorrowRecordTest.java
class BorrowRecordTest {

    private BorrowRecord record;
    private Book book;
    private User user;

    @BeforeEach
    void setUp() {
        // نستخدم كائنات حقيقية (حتى لو بيانات وهمية) لضمان أن toString يعمل بشكل أجمل
        book = new Book("Title", "Author", "ISBN");
        user = new User("User", "Pass", "Role");
        record = new BorrowRecord(book, user);
    }

    // ---------------------------------------------------
    // 1. اختبار الـ Getters الأساسية
    // ---------------------------------------------------
    @Test
    @DisplayName("Test All Getters")
    void testGetters() {
        assertNotNull(record.getBook());
        assertNotNull(record.getUser());
        assertNotNull(record.getBorrowDate());
        assertNotNull(record.getDueDate());
        
        // التأكد من أن التواريخ منطقية (تاريخ الاستحقاق بعد تاريخ الاستعارة)
        assertTrue(record.getDueDate().isAfter(record.getBorrowDate()));
    }

    // ---------------------------------------------------
    // 2. اختبار المنطق الافتراضي للكونستركتور
    // ---------------------------------------------------
    @Test
    @DisplayName("Test Constructor Defaults")
    void testConstructorDefaults() {
        // الافتراضي يجب أن يكون false
        assertFalse(record.isReturned());
        
        // الافتراضي لمدة الإعارة 28 يوم
        assertEquals(LocalDate.now().plusDays(28), record.getDueDate());
    }

    // ---------------------------------------------------
    // 3. اختبار تغيير حالة الإرجاع (Setter)
    // ---------------------------------------------------
    @Test
    @DisplayName("Test setReturned")
    void testSetReturned() {
        record.setReturned(true);
        assertTrue(record.isReturned());
        
        record.setReturned(false);
        assertFalse(record.isReturned());
    }

    // ---------------------------------------------------
    // 4. اختبار منطق التأخير (isOverdue) باستخدام Reflection
    // ---------------------------------------------------
    @Test
    @DisplayName("Test isOverdue Logic (Time Travel)")
    void testIsOverdue() throws Exception {
        // أ. الحالة الطبيعية: الكتاب معاه وقت
        assertFalse(record.isOverdue());

        // ب. حالة التأخير: نغير تاريخ الاستحقاق ليصبح بالأمس
        modifyDueDate(record, LocalDate.now().minusDays(1));
        assertTrue(record.isOverdue());

        // ج. حالة الكتاب المرجع: حتى لو الوقت متأخر، إذا رجعه لا يعتبر Overdue
        record.setReturned(true);
        assertFalse(record.isOverdue());
    }

    // ---------------------------------------------------
    // 5. اختبار حساب أيام التأخير (getDaysOverdue)
    // ---------------------------------------------------
    @Test
    @DisplayName("Test getDaysOverdue Calculation")
    void testDaysOverdue() throws Exception {
        // أ. ليس متأخراً -> يجب أن يرجع 0
        assertEquals(0, record.getDaysOverdue());

        // ب. متأخر 5 أيام
        modifyDueDate(record, LocalDate.now().minusDays(5));
        
        // نتأكد أولاً أنه يعتبر متأخراً
        assertTrue(record.isOverdue());
        // نتأكد من الرقم
        assertEquals(5, record.getDaysOverdue());
    }

    // ---------------------------------------------------
    // 6. اختبار toString
    // ---------------------------------------------------
    @Test
    @DisplayName("Test toString")
    void testToString() {
        String result = record.toString();
        assertNotNull(result);
        // نتأكد أن القيم المهمة موجودة في النص
        assertTrue(result.contains("borrowDate"));
        assertTrue(result.contains("dueDate"));
        assertTrue(result.contains("returned=false"));
    }

    // ==========================================
    // دالة مساعدة لتغيير التاريخ (Reflection)
    // ==========================================
    private void modifyDueDate(BorrowRecord target, LocalDate newDate) throws Exception {
        // نستخدم الـ Reflection للوصول للمتغير الخاص dueDate وتعديله للاختبار
        Field field = BorrowRecord.class.getDeclaredField("dueDate");
        field.setAccessible(true);
        field.set(target, newDate);
    }
}