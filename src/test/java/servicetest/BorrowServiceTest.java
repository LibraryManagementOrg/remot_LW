package servicetest;


import model.Book;
import model.BorrowRecord;
import model.User;
import service.BorrowService;
import service.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BorrowServiceTest {

    private BorrowService borrowService;
    private UserService stubUserService;
    private User validUser;
    private Book book;

    @BeforeEach
    void setUp() {
        validUser = new User("Alice", "pass", "User");
        book = new Book("Java Guide", "Author", "12345");

        // --- Stubbing UserService ---
        // نقوم بإنشاء UserService وهمي يرجع دائماً "validUser" كمستخدم مسجل دخوله
        stubUserService = new UserService() {
            @Override
            public User getLoggedInUser() {
                return validUser;
            }
        };

        borrowService = new BorrowService(stubUserService);
    }

    @Test
    @DisplayName("1. Test Borrow Success")
    void testBorrowBookSuccess() {
        // محاولة استعارة صحيحة
        BorrowRecord record = borrowService.borrowBook(validUser, book);

        assertNotNull(record, "Borrow record should be created");
        assertTrue(book.isBorrowed(), "Book status should change to borrowed");
        assertEquals(1, borrowService.getAllRecords().size(), "Record list should have 1 item");
    }

    @Test
    @DisplayName("2. Test Borrow Fail: User Not Logged In")
    void testBorrowFailNotLoggedIn() {
        // نغير الـ Stub ليرجع مستخدم آخر أو null
        UserService loggedOutService = new UserService() {
            @Override
            public User getLoggedInUser() {
                return null; // لا يوجد مستخدم مسجل
            }
        };
        BorrowService service = new BorrowService(loggedOutService);

        BorrowRecord record = service.borrowBook(validUser, book);

        assertNull(record, "Should fail if user is not logged in");
        assertFalse(book.isBorrowed());
    }

    @Test
    @DisplayName("3. Test Borrow Fail: Different User Logged In")
    void testBorrowFailWrongUser() {
        // نغير الـ Stub ليرجع مستخدم غير الذي يحاول الاستعارة
        UserService wrongUserService = new UserService() {
            @Override
            public User getLoggedInUser() {
                return new User("Hacker", "pass", "User");
            }
        };
        BorrowService service = new BorrowService(wrongUserService);

        BorrowRecord record = service.borrowBook(validUser, book);

        assertNull(record, "Should fail if trying to borrow for another user");
        assertFalse(book.isBorrowed());
    }

    @Test
    @DisplayName("4. Test Borrow Fail: Book Already Borrowed")
    void testBorrowFailBookUnavailable() {
        // نجعل الكتاب مستعاراً مسبقاً
        book.setBorrowed(true);

        BorrowRecord record = borrowService.borrowBook(validUser, book);

        assertNull(record, "Should fail if book is already borrowed");
    }

    @Test
    @DisplayName("5. Test Borrow Fail: User Has Fines")
    void testBorrowFailUserHasFines() {
        // إضافة غرامة للمستخدم
        validUser.setOutstandingFine(50.0);

        BorrowRecord record = borrowService.borrowBook(validUser, book);

        assertNull(record, "Should fail if user has unpaid fines");
        assertFalse(book.isBorrowed());
    }

    @Test
    @DisplayName("6. Test Get Overdue Books")
    void testGetOverdueBooks() throws Exception {
        // 1. استعارة كتاب بنجاح
        BorrowRecord record = borrowService.borrowBook(validUser, book);
        assertNotNull(record);

        // 2. استخدام Reflection لتعديل تاريخ الاستحقاق وجعله في الماضي
        // (لأن السجل الجديد لا يكون متأخراً افتراضياً)
        modifyDueDate(record, LocalDate.now().minusDays(5));

        // 3. التحقق من القائمة
        List<BorrowRecord> overdueList = borrowService.getOverdueBooks();
        
        assertEquals(1, overdueList.size(), "Should find 1 overdue book");
        assertEquals(record, overdueList.get(0));
    }

    // --- دالة مساعدة لتعديل التاريخ في BorrowRecord (Reflection) ---
    private void modifyDueDate(BorrowRecord record, LocalDate newDate) throws Exception {
        Field field = BorrowRecord.class.getDeclaredField("dueDate");
        field.setAccessible(true);
        field.set(record, newDate);
    }
}