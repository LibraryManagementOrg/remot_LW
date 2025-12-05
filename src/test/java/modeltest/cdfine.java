package modeltest;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import model.CDFineStrategy;
import org.junit.jupiter.api.DisplayName;

	class cdfine {

	    @Test
	    @DisplayName("Test CD Fine Calculation: Should multiply overdue days by 20.0")
	    void testCalculateFine() {
	        // إنشاء الكائن
	        CDFineStrategy strategy = new CDFineStrategy();

	        // الحالة 1: تأخير 3 أيام (المتوقع 3 * 20 = 60.0)
	        double result1 = strategy.calculateFine(3);
	        assertEquals(60.0, result1, 0.001, "Fine for 3 days should be 60.0");

	        // الحالة 2: تأخير يوم واحد (المتوقع 20.0)
	        double result2 = strategy.calculateFine(1);
	        assertEquals(20.0, result2, 0.001, "Fine for 1 day should be 20.0");

	        // الحالة 3: صفر أيام (المتوقع 0.0)
	        double result3 = strategy.calculateFine(0);
	        assertEquals(0.0, result3, 0.001, "Fine for 0 days should be 0.0");
	        
	        // الحالة 4: عدد أيام كبير (مثلاً 10 أيام -> 200.0)
	        assertEquals(200.0, strategy.calculateFine(10), 0.001);
	    }
	
}
