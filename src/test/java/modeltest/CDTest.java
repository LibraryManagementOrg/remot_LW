package modeltest;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import model.CD;

class CDTest {

    private CD cd;

    @BeforeEach
    void setUp() {
        cd = new CD("Thriller", "Michael Jackson", "111-222-333");
    }

    // ---------------------------------------------------------
    // 1. اختبار الـ Constructor والربط مع الأب (Super)
    // ---------------------------------------------------------
    @Test
    @DisplayName("Test Constructor Mapping (Artist -> Creator, Barcode -> ID)")
    void testConstructorAndMapping() {
        // فحص أن البيانات انتقلت للأب بشكل صحيح
        assertEquals("Thriller", cd.getTitle());
        assertEquals("Michael Jackson", cd.getCreator()); // Super getter
        assertEquals("111-222-333", cd.getId());       // Super getter
    }

    // ---------------------------------------------------------
    // 2. اختبار الـ Getters الخاصة (getArtist, getBarcode)
    // ---------------------------------------------------------
    @Test
    @DisplayName("Test Specific Getters")
    void testSpecificGetters() {
        // فحص الدوال الخاصة بـ CD
        assertEquals("Michael Jackson", cd.getArtist());
        assertEquals("111-222-333", cd.getBarcode());
    }

    // ---------------------------------------------------------
    // 3. اختبار الثوابت (Overrides)
    // ---------------------------------------------------------
    @Test
    @DisplayName("Test Loan Period and Daily Fine Constants")
    void testConstants() {
        // حسب المتطلبات في الكود: 7 أيام و 20 شيكل
        assertEquals(7, cd.getLoanPeriod());
        assertEquals(20.0, cd.getDailyFine());
    }

    // ---------------------------------------------------------
    // 4. اختبار تكامل الاستراتيجية (Strategy Pattern Integration)
    // ---------------------------------------------------------
    @Test
    @DisplayName("Test Fine Strategy Integration")
    void testStrategyIntegration() {
        // هذا الاختبار يضمن أن سطر this.setFineStrategy(...) في الكونستركتور تم تنفيذه
        
        cd.setBorrowed(true);
        // نؤخر التاريخ يومين
        cd.setDueDate(LocalDate.now().minusDays(2));
        
        // الحسبة: 2 يوم * 20 (قيمة الغرامة في CD) = 40
        // إذا رجع 40، فهذا يعني أن الاستراتيجية تم تعيينها بشكل صحيح
        assertEquals(40.0, cd.getFineAmount());
    }

    // ---------------------------------------------------------
    // 5. اختبار toString
    // ---------------------------------------------------------
    @Test
    @DisplayName("Test toString Format")
    void testToString() {
        String result = cd.toString();
        
        assertNotNull(result);
        assertTrue(result.contains("CD: Thriller"));
        assertTrue(result.contains("Artist: Michael Jackson"));
        assertTrue(result.contains("Barcode: 111-222-333"));
    }
}