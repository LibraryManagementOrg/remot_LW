package modeltest;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import model.CD;
import model.User;

import java.time.LocalDate;

class CDtest {

    private CD cd;
    private User user;

    @BeforeEach
    void setUp() {
        // تهيئة البيانات قبل كل اختبار
        cd = new CD("Thriller", "Michael Jackson", "111-222-333");
        user = new User("John", "password", "User");
    }

    @Test
    @DisplayName("1. Test Constructor & Aliases")
    void testConstructorAndAliases() {
        // فحص البيانات الأساسية
        assertEquals("Thriller", cd.getTitle());
        
        // فحص دوال الـ Aliases الخاصة بالـ CD
        assertEquals("Michael Jackson", cd.getArtist()); // يتأكد أن getArtist تستدعي getCreator
        assertEquals("111-222-333", cd.getBarcode());  // يتأكد أن getBarcode تستدعي getId
        
        // التأكد من أن getCreator و getId الأصلية تعملان أيضاً
        assertEquals("Michael Jackson", cd.getCreator());
        assertEquals("111-222-333", cd.getId());
    }

    @Test
    @DisplayName("2. Test CD Specific Configuration (Sprint 5 Requirements)")
    void testCDConfiguration() {
        // التأكد من مدة الإعارة الخاصة بالـ CD
        assertEquals(7, cd.getLoanPeriod(), "Loan period for CD should be 7 days");
        
        // التأكد من الغرامة اليومية الخاصة بالـ CD
        assertEquals(20.0, cd.getDailyFine(), "Daily fine for CD should be 20.0");
    }

    @Test
    @DisplayName("3. Test Inheritance logic (Setters/Getters from Media)")
    void testInheritanceLogic() {
        // بما أن CD يرث من media، يجب أن نتمكن من استخدام دوال media
        assertFalse(cd.isBorrowed());
        
        // تغيير الحالة يدوياً (لأن CD لا يملك دالة borrow خاصة به في الكود المرفق)
        cd.setBorrowed(true);
        cd.setBorrowedBy(user);
        
        assertTrue(cd.isBorrowed());
        assertEquals(user, cd.getBorrowedBy());
    }

    @Test
    @DisplayName("4. Test Fine Calculation Strategy")
    void testFineCalculation() {
        // 1. محاكاة أن الـ CD مستعار
        cd.setBorrowed(true);
        
        // 2. محاكاة أنه متأخر بـ 3 أيام
        // (نضع تاريخ الاستحقاق قبل 3 أيام من اليوم)
        LocalDate overdueDate = LocalDate.now().minusDays(3);
        cd.setDueDate(overdueDate);

        // 3. التحقق من أن الـ CD يعتبر متأخراً
        assertTrue(cd.isOverdue());

        // 4. التحقق من الحسبة: 3 أيام * 20 شيكل = 60 شيكل
        // (يعتمد هذا على أن CDFineStrategy تعمل بشكل صحيح وتضرب في 20)
        double expectedFine = 3 * 20.0;
        
        assertEquals(expectedFine, cd.getFineAmount(), 0.01, "Fine should be calculated using CD strategy (20 * days)");
    }
    
    @Test
    @DisplayName("5. Test toString")
    void testToString() {
        String result = cd.toString();
        assertTrue(result.contains("CD: Thriller"));
        assertTrue(result.contains("Artist: Michael Jackson"));
        assertTrue(result.contains("Barcode: 111-222-333"));
    }
}