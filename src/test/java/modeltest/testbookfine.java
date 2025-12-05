package modeltest;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import model.BookFineStrategy;


class testbookfine {

    @Test
    @DisplayName("Test Calculate Fine: Should multiply overdue days by 10.0")
    void testCalculateFine() {
        // إنشاء الكائن المراد اختباره
        BookFineStrategy strategy = new BookFineStrategy();

        // الحالة 1: تأخير 5 أيام (المتوقع 50.0)
        double result1 = strategy.calculateFine(5);
        assertEquals(50.0, result1, 0.001, "Fine for 5 days should be 50.0");

        // الحالة 2: تأخير يوم واحد (المتوقع 10.0)
        double result2 = strategy.calculateFine(1);
        assertEquals(10.0, result2, 0.001, "Fine for 1 day should be 10.0");

        // الحالة 3: عدم وجود تأخير (0 أيام -> المتوقع 0.0)
        double result3 = strategy.calculateFine(0);
        assertEquals(0.0, result3, 0.001, "Fine for 0 days should be 0.0");
        
        // الحالة 4: عدد أيام كبير (مثلاً 100 يوم -> المتوقع 1000.0)
        assertEquals(1000.0, strategy.calculateFine(100), "Fine for 100 days should be 1000.0");
    }
}
