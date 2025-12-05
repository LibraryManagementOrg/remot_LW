package servicetest;
import model.Book;
import model.BorrowRecord;
import model.User;
import service.FineService;
import service.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FineServiceTest {

    private FineService fineService;
    private UserService stubUserService;
    private User currentUser;
    private Book book;
    private BorrowRecord record;

    // متغير للتحكم في حالة تسجيل الدخول الوهمية
    private User loggedInUserForTesting;

    @BeforeEach
    void setUp() {
        currentUser = new User("Alice", "pass", "User");
        book = new Book("Java Book", "Author", "123");
        record = new BorrowRecord(book, currentUser);

        // --- Stubbing UserService ---
        // ننشئ نسخة وهمية تتحكم في من هو المستخدم المسجل حالياً
        stubUserService = new UserService() {
            @Override
            public boolean isLoggedIn() {
                return loggedInUserForTesting != null;
            }

            @Override
            public User getLoggedInUser() {
                return loggedInUserForTesting;
            }
        };

        fineService = new FineService(stubUserService);
    }

    @Test
    @DisplayName("1. Test Calculate Fine Logic")
    void testCalculateFine() throws Exception {
        // جعل السجل متأخراً بـ 5 أيام باستخدام Reflection
        // الغرامة اليومية في الكلاس هي 1.0
        modifyDueDate(record, LocalDate.now().minusDays(5));

        double fine = fineService.calculateFine(record);
        
        // المتوقع: 5 أيام * 1.0 = 5.0
        assertEquals(5.0, fine, 0.001);
    }

    @Test
    @DisplayName("2. Test Apply Fine (Success: User Logged In)")
    void testApplyFineSuccess() throws Exception {
        // تسجيل دخول المستخدم الصحيح
        loggedInUserForTesting = currentUser;

        // تأخير 10 أيام -> غرامة 10.0
        modifyDueDate(record, LocalDate.now().minusDays(10));

        fineService.applyFine(currentUser, record);

        assertEquals(10.0, currentUser.getOutstandingFine(), "Fine should be applied to user account");
    }

    @Test
    @DisplayName("3. Test Apply Fine (Fail: Access Denied)")
    void testApplyFineAccessDenied() throws Exception {
        // سيناريو 1: لم يسجل دخول أحد
        loggedInUserForTesting = null;
        
        modifyDueDate(record, LocalDate.now().minusDays(5));
        fineService.applyFine(currentUser, record);
        assertEquals(0.0, currentUser.getOutstandingFine(), "Should not apply fine if not logged in");

        // سيناريو 2: مستخدم آخر مسجل دخول
        loggedInUserForTesting = new User("Hacker", "pass", "User");
        fineService.applyFine(currentUser, record);
        assertEquals(0.0, currentUser.getOutstandingFine(), "Should not apply fine if wrong user is logged in");
    }

    @Test
    @DisplayName("4. Test Pay Fine (Success)")
    void testPayFineSuccess() {
        loggedInUserForTesting = currentUser;
        
        // وضع غرامة مبدئية 50.0
        currentUser.setOutstandingFine(50.0);

        // دفع 20.0
        fineService.payFine(currentUser, 20.0);

        // المتبقي يجب أن يكون 30.0
        assertEquals(30.0, currentUser.getOutstandingFine());
    }

    @Test
    @DisplayName("5. Test Pay Fine (Overpayment checks)")
    void testPayFineOverpayment() {
        loggedInUserForTesting = currentUser;
        currentUser.setOutstandingFine(10.0);

        // دفع 20.0 (أكثر من المستحق)
        fineService.payFine(currentUser, 20.0);

        // الرصيد يجب أن يصبح 0.0 (لا ينزل للسالب)
        assertEquals(0.0, currentUser.getOutstandingFine());
    }

    @Test
    @DisplayName("6. Test Pay Fine (Fail: Access Denied)")
    void testPayFineAccessDenied() {
        currentUser.setOutstandingFine(100.0);

        // مستخدم غير مسجل دخول
        loggedInUserForTesting = null;

        fineService.payFine(currentUser, 50.0);

        // الرصيد يجب أن يبقى كما هو
        assertEquals(100.0, currentUser.getOutstandingFine());
    }

    // --- دالة مساعدة لتعديل التاريخ (Reflection) ---
    private void modifyDueDate(BorrowRecord record, LocalDate newDate) throws Exception {
        Field field = BorrowRecord.class.getDeclaredField("dueDate");
        field.setAccessible(true);
        field.set(record, newDate);
    }
}