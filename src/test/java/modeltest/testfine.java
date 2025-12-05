package modeltest;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import model.FineStrategy;

import org.junit.jupiter.api.DisplayName;

class testfine {

    @Test
    @DisplayName("Test Interface Implementation using Lambda")
    void testInterfaceContract() {
        // بما أن FineStrategy هي Functional Interface (تحتوي دالة واحدة)
        // يمكننا عمل تنفيذ بسيط لها باستخدام Lambda للتجربة
        
        // سنفترض استراتيجية تجريبية: الغرامة = عدد الأيام * 1
        FineStrategy testStrategy = (days) -> days * 1.0;

        // التحقق من أن الدالة تعمل وتقبل long وترجع double
        assertEquals(5.0, testStrategy.calculateFine(5));
        assertEquals(0.0, testStrategy.calculateFine(0));
    }

    @Test
    @DisplayName("Test Interface using Anonymous Class")
    void testAnonymousImplementation() {
        // طريقة أخرى: إنشاء كلاس مؤقت داخل التست
        FineStrategy fixedFineStrategy = new FineStrategy() {
            @Override
            public double calculateFine(long overdueDays) {
                return 100.0; // غرامة ثابتة مهما كان عدد الأيام
            }
        };

        assertEquals(100.0, fixedFineStrategy.calculateFine(1));
        assertEquals(100.0, fixedFineStrategy.calculateFine(50));
    }
}
