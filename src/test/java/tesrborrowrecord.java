import model.*;
import static org.junit.jupiter.api.Assertions.*;
import java.lang.reflect.Field;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BorrowRecordTest {

    @Mock
    private Book bookMock;
    
    @Mock
    private User userMock;

    private BorrowRecord borrowRecord;

    @BeforeEach
    void setUp() {
        // يتم إنشاء سجل جديد قبل كل اختبار
        // في الوضع الطبيعي: DueDate = Today + 28 days
        borrowRecord = new BorrowRecord(bookMock, userMock);
    }

    @Test
    void testInitialization() {
        // التأكد من أن القيم الأولية صحيحة
        assertNotNull(borrowRecord.getBook());
        assertNotNull(borrowRecord.getUser());
        assertEquals(LocalDate.now(), borrowRecord.getBorrowDate(), "Borrow date should be today");
        assertEquals(LocalDate.now().plusDays(28), borrowRecord.getDueDate(), "Due date should be 28 days from now");
        assertFalse(borrowRecord.isReturned(), "Book should not be returned initially");
    }

    @Test
    void testSetReturned() {
        // اختبار عملية الإرجاع
        borrowRecord.setReturned(true);
        assertTrue(borrowRecord.isReturned());
    }

    @Test
    void testIsOverdue_NotOverdue() {
        // الحالة الطبيعية: الكتاب تم استعارته اليوم، لذا هو ليس متأخراً
        assertFalse(borrowRecord.isOverdue());
    }

    @Test
    void testIsOverdue_True_UsingReflection() throws Exception {
        // 🔴 خدعة: نستخدم Reflection لتغيير تاريخ الإرجاع لجعله في الماضي
        // نجعل تاريخ الإرجاع (أمس)
        setPrivateDateField(borrowRecord, "dueDate", LocalDate.now().minusDays(1));

        // الآن يجب أن يكون متأخراً
        assertTrue(borrowRecord.isOverdue(), "Should be overdue because due date is in the past");
    }
    
    @Test
    void testIsOverdue_False_IfReturned() throws Exception {
        // حتى لو التاريخ فات، إذا تم الإرجاع لا يعتبر متأخراً
        setPrivateDateField(borrowRecord, "dueDate", LocalDate.now().minusDays(5));
        borrowRecord.setReturned(true);

        assertFalse(borrowRecord.isOverdue(), "Should not be overdue if returned, even if date passed");
    }

    @Test
    void testGetDaysOverdue() throws Exception {
        // 1. في البداية لا يوجد تأخير
        assertEquals(0, borrowRecord.getDaysOverdue());

        // 2. نجعل تاريخ الإرجاع قبل 5 أيام من اليوم
        setPrivateDateField(borrowRecord, "dueDate", LocalDate.now().minusDays(5));

        // 3. نتوقع أن يكون التأخير 5 أيام
        assertEquals(5, borrowRecord.getDaysOverdue(), "Should calculate 5 days of overdue");
    }

    @Test
    void testToString() {
        String result = borrowRecord.toString();
        assertTrue(result.contains("BorrowRecord"));
        assertTrue(result.contains("returned=false"));
    }

    // =======================================================
    // 🔧 دالة مساعدة لتغيير الحقول الـ Private (Reflection Helper)
    // =======================================================
    private void setPrivateDateField(Object targetObject, String fieldName, LocalDate newValue) throws Exception {
        Field field = targetObject.getClass().getDeclaredField(fieldName);
        field.setAccessible(true); // السماح بالوصول للمتغير Private
        field.set(targetObject, newValue); // تغيير القيمة
    }
}