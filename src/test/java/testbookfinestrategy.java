import model.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class BookFineStrategyTest {

    @Test
    void testCalculateFine_NormalCase() {
        // 1. تجهيز (Arrange)
        BookFineStrategy strategy = new BookFineStrategy();
        long overdueDays = 5; // 5 أيام تأخير

        // 2. تنفيذ (Act)
        double fine = strategy.calculateFine(overdueDays);

        // 3. تحقق (Assert)
        // المتوقع: 5 * 10 = 50.0
        assertEquals(50.0, fine, "Fine should be 10 NIS per day for books");
    }

    @Test
    void testCalculateFine_ZeroDays() {
        BookFineStrategy strategy = new BookFineStrategy();
        
        // إذا لم يكن هناك تأخير (0 أيام)، الغرامة يجب أن تكون 0
        assertEquals(0.0, strategy.calculateFine(0));
    }

    @Test
    void testCalculateFine_OneDay() {
        BookFineStrategy strategy = new BookFineStrategy();
        
        // يوم واحد تأخير = 10 شيكل
        assertEquals(10.0, strategy.calculateFine(1));
    }
}