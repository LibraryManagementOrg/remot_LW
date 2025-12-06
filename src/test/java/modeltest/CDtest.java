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
        // تهيئة كائن جديد قبل كل اختبار لضمان بيئة نظيفة
        cd = new CD("Thriller", "Michael Jackson", "111-222-333");
        user = new User("John", "password", "User");
    }

    @Test
    @DisplayName("Test Constructor and Superclass Mapping")
    void testConstructorAndMapping() {
        // التحقق من القيم المباشرة
        assertEquals("Thriller", cd.getTitle());
        
        // التحقق من ربط الـ Artist بـ Creator
        assertEquals("Michael Jackson", cd.getArtist());
        assertEquals("Michael Jackson", cd.getCreator());
        
        // التحقق من ربط الـ Barcode بـ ID
        assertEquals("111-222-333", cd.getBarcode());
        assertEquals("111-222-333", cd.getId());
    }

    @Test
    @DisplayName("Test CD Specific Constants (Loan Period & Daily Fine)")
    void testCDConstants() {
        // اختبار القواعد الخاصة بالسي دي (Sprint 5)
        assertEquals(7, cd.getLoanPeriod(), "CD loan period must be 7 days");
        assertEquals(20.0, cd.getDailyFine(), "CD daily fine must be 20.0");
    }

    @Test
    @DisplayName("Test Inheritance Behavior (Borrowing)")
    void testInheritanceBehavior() {
        // التأكد من الحالة الافتراضية
        assertFalse(cd.isBorrowed());
        assertNull(cd.getBorrowedBy());

        // تغيير الحالة واختبار الاستجابة
        cd.setBorrowed(true);
        cd.setBorrowedBy(user);
        
        assertTrue(cd.isBorrowed());
        assertEquals(user, cd.getBorrowedBy());
    }

    @Test
    @DisplayName("Test Fine Calculation: OVERDUE Case")
    void testFineCalculationOverdue() {
        cd.setBorrowed(true);
        // جعل تاريخ الاستحقاق قبل 5 أيام من الآن
        cd.setDueDate(LocalDate.now().minusDays(5));

        assertTrue(cd.isOverdue(), "Should be overdue");
        
        // الحسبة: 5 أيام * 20 شيكل = 100
        assertEquals(100.0, cd.getFineAmount(), 0.001, "Fine should be 5 * 20 = 100");
    }

    @Test
    @DisplayName("Test Fine Calculation: NOT OVERDUE (Future Date)")
    void testFineCalculationNotOverdue() {
        cd.setBorrowed(true);
        // جعل تاريخ الاستحقاق غداً
        cd.setDueDate(LocalDate.now().plusDays(1));

        assertFalse(cd.isOverdue(), "Should not be overdue");
        assertEquals(0.0, cd.getFineAmount(), "Fine should be 0 for future due date");
    }

    @Test
    @DisplayName("Test Fine Calculation: NOT OVERDUE (Due Today)")
    void testFineCalculationDueToday() {
        cd.setBorrowed(true);
        // تاريخ الاستحقاق هو اليوم
        cd.setDueDate(LocalDate.now());

        assertFalse(cd.isOverdue(), "Should not be overdue if returned on the due date");
        assertEquals(0.0, cd.getFineAmount(), "Fine should be 0 if returned today");
    }
    
    @Test
    @DisplayName("Test toString Formatting")
    void testToString() {
        String output = cd.toString();
        
        assertNotNull(output);
        // التحقق من وجود الكلمات المفتاحية والقيم
        assertTrue(output.contains("CD: Thriller"));
        assertTrue(output.contains("Artist: Michael Jackson"));
        assertTrue(output.contains("Barcode: 111-222-333"));
    }
}