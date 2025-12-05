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
    // سنمرر null للكتاب والمستخدم لأن كودك الحالي لا يستخدم دوالهم الداخلية
    // وهذا يسهل الاختبار دون الحاجة لكلاسات Book و User
    private Book book = null; 
    private User user = null;

    @BeforeEach
    void setUp() throws Exception {
        // يتم إنشاء سجل جديد قبل كل اختبار
        record = new BorrowRecord(book, user);
    }

    @Test
    @DisplayName("Test 1: Constructor sets correct initial dates")
    void testConstructorDefaults() {
        assertNotNull(record.getBorrowDate());
        assertNotNull(record.getDueDate());
        
        // التأكد من أن تاريخ الإعارة هو اليوم
        assertEquals(LocalDate.now(), record.getBorrowDate());
        
        // التأكد من أن تاريخ الاستحقاق بعد 28 يوم
        assertEquals(LocalDate.now().plusDays(28), record.getDueDate());
        
        // التأكد من أن الكتاب غير مرجع
        assertFalse(record.isReturned());
    }

    @Test
    @DisplayName("Test 2: setReturned updates status correctly")
    void testSetReturned() {
        record.setReturned(true);
        assertTrue(record.isReturned());
        
        record.setReturned(false);
        assertFalse(record.isReturned());
    }

    @Test
    @DisplayName("Test 3: isOverdue is false for new records")
    void testIsOverdue_InitiallyFalse() {
        // السجل الجديد تاريخ استحقاقه في المستقبل، لذا لا يجب أن يكون متأخراً
        assertFalse(record.isOverdue());
        assertEquals(0, record.getDaysOverdue());
    }

    @Test
    @DisplayName("Test 4: isOverdue is TRUE when due date is in the past")
    void testIsOverdue_True() throws Exception {
        // خدعة الـ Reflection:
        // سنقوم بتغيير تاريخ الاستحقاق يدوياً ليكون الأمس
        LocalDate yesterday = LocalDate.now().minusDays(1);
        modifyDueDate(record, yesterday);

        // الآن يجب أن يكون الكتاب متأخراً
        assertTrue(record.isOverdue(), "Should be overdue because due date was passed");
    }

    @Test
    @DisplayName("Test 5: Days Overdue calculation is correct")
    void testGetDaysOverdue() throws Exception {
        // خدعة الـ Reflection:
        // نضع تاريخ الاستحقاق قبل 10 أيام من الآن
        LocalDate tenDaysAgo = LocalDate.now().minusDays(10);
        modifyDueDate(record, tenDaysAgo);

        assertTrue(record.isOverdue());
        // يجب أن يرجع 10 أيام
        assertEquals(10, record.getDaysOverdue());
    }

    @Test
    @DisplayName("Test 6: isOverdue is FALSE if returned, even if late")
    void testIsOverdue_ReturnedButLate() throws Exception {
        // نضع تاريخ الاستحقاق في الماضي (متأخر)
        LocalDate pastDate = LocalDate.now().minusDays(5);
        modifyDueDate(record, pastDate);

        // لكن المستخدم أرجع الكتاب
        record.setReturned(true);

        // النتيجة: غير متأخر لأن الكتاب تم إرجاعه
        assertFalse(record.isOverdue());
    }

    @Test
    @DisplayName("Test 7: toString contains essential data")
    void testToString() {
        String result = record.toString();
        assertTrue(result.contains("borrowDate="));
        assertTrue(result.contains("dueDate="));
        assertTrue(result.contains("returned="));
    }

    // -----------------------------------------------------------
    // دالة مساعدة (Helper Method) لتعديل الحقول الـ private
    // -----------------------------------------------------------
    private void modifyDueDate(BorrowRecord targetRecord, LocalDate newDate) throws Exception {
        // الوصول لحقل dueDate داخل الكلاس
        Field field = BorrowRecord.class.getDeclaredField("dueDate");
        
        // السماح بتعديله (لأنه private)
        field.setAccessible(true);
        
        // وضع القيمة الجديدة
        field.set(targetRecord, newDate);
    }
}
