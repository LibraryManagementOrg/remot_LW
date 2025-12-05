package modeltest;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import model.User;
import org.junit.jupiter.api.DisplayName;

class UserTest {

    private User user;

    @BeforeEach
    void setUp() {
        // تهيئة مستخدم باستخدام الكونستركتور القديم (الحالة الشائعة)
        user = new User("Alice", "password123", "User");
    }

    @Test
    @DisplayName("1. Test Legacy Constructor (Defaults)")
    void testLegacyConstructor() {
        assertEquals("Alice", user.getName());
        assertEquals("password123", user.getPassword());
        assertEquals("User", user.getRole());
        
        // التأكد من القيم الافتراضية
        assertEquals(0.0, user.getOutstandingFine(), "Default fine should be 0.0");
        assertEquals("", user.getEmail(), "Default email should be empty string");
    }

    @Test
    @DisplayName("2. Test Full Constructor")
    void testFullConstructor() {
        // إنشاء مستخدم باستخدام الكونستركتور الجديد
        User fullUser = new User("Bob", "pass456", "Admin", 50.0, "bob@example.com");

        assertEquals("Bob", fullUser.getName());
        assertEquals(50.0, fullUser.getOutstandingFine());
        assertEquals("bob@example.com", fullUser.getEmail());
    }

    @Test
    @DisplayName("3. Test Setters and Getters")
    void testSettersAndGetters() {
        user.setEmail("alice@gmail.com");
        user.setOutstandingFine(20.0);

        assertEquals("alice@gmail.com", user.getEmail());
        assertEquals(20.0, user.getOutstandingFine());
    }

    @Test
    @DisplayName("4. Test Add Fine Logic")
    void testAddFine() {
        // إضافة غرامة عادية
        user.addFine(10.0);
        assertEquals(10.0, user.getOutstandingFine());

        // إضافة غرامة أخرى
        user.addFine(5.5);
        assertEquals(15.5, user.getOutstandingFine());

        // محاولة إضافة رقم سالب أو صفر (يجب أن يتم تجاهله)
        user.addFine(-10.0);
        user.addFine(0);
        assertEquals(15.5, user.getOutstandingFine(), "Should ignore non-positive amounts");
    }

    @Test
    @DisplayName("5. Test Pay Fine Logic (Partial & Full Payment)")
    void testPayFine() {
        // إعداد: نضع غرامة 100
        user.setOutstandingFine(100.0);

        // دفع جزئي
        user.payFine(40.0);
        assertEquals(60.0, user.getOutstandingFine(), "100 - 40 should be 60");

        // دفع كامل للمبلغ المتبقي
        user.payFine(60.0);
        assertEquals(0.0, user.getOutstandingFine(), "Should be 0 after full payment");
    }

    @Test
    @DisplayName("6. Test Pay Fine Logic (Overpayment & Invalid)")
    void testPayFineEdgeCases() {
        user.setOutstandingFine(50.0);

        // محاولة دفع مبلغ سالب (يجب تجاهله)
        user.payFine(-20.0);
        assertEquals(50.0, user.getOutstandingFine(), "Should ignore negative payment");

        // دفع مبلغ أكبر من الغرامة (يجب أن يصبح الرصيد 0 ولا ينزل للسالب)
        user.payFine(100.0);
        assertEquals(0.0, user.getOutstandingFine(), "Balance should not go below zero");
    }

    @Test
    @DisplayName("7. Test canBorrow Logic")
    void testCanBorrow() {
        // الحالة 1: الغرامة 0 (يسمح بالاستعارة)
        user.setOutstandingFine(0.0);
        assertTrue(user.canBorrow(), "Should be able to borrow if fines are 0");

        // الحالة 2: الغرامة > 0 (يمنع الاستعارة)
        user.setOutstandingFine(0.1); // حتى لو مبلغ صغير جداً
        assertFalse(user.canBorrow(), "Should NOT be able to borrow if fines exist");
    }

    @Test
    @DisplayName("8. Test toString")
    void testToString() {
        user.setEmail("test@mail.com");
        String result = user.toString();

        assertTrue(result.contains("Alice"));
        assertTrue(result.contains("User"));
        assertTrue(result.contains("test@mail.com")); // التأكد من ظهور الإيميل
    }
}