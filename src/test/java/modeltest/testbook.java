package modeltest;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import model.User;
import model.Book;

import java.time.LocalDate;

class testbook {

    private Book book;
    private User user;

    @BeforeEach
    void setUp() {
        user = new User("Alice", "password123", "User");
        book = new Book("Clean Code", "Robert C. Martin", "978-0132350884");
    }

    // ============================================
    // 1. اختبار الأساسيات والتوافق
    // ============================================
    @Test
    @DisplayName("Test Constructor and Basic Getters")
    void testBasicProperties() {
        assertAll("Basic Checks",
            () -> assertEquals("Clean Code", book.getTitle()),
            () -> assertEquals("Robert C. Martin", book.getCreator()),
            () -> assertEquals("978-0132350884", book.getId()),
            () -> assertEquals("Robert C. Martin", book.getAuthor()),
            () -> assertEquals(28, book.getLoanPeriod()),
            () -> assertEquals(10.0, book.getDailyFine())
        );
        
        // تغطية دالة setFineAmount الفارغة
        book.setFineAmount(50.0);
    }
    
    // ... (باقي الاختبارات كما هي لتغطية الشروط الأخرى)

    // ============================================
    // 2. اختبار الاستعارة (لتغطية شرط if في borrow)
    // ============================================
    @Test
    @DisplayName("Test Borrowing Flow (Success and Failure)")
    void testBorrowFlow() {
        // حالة نجاح (if = False)
        book.borrow(user);
        assertTrue(book.isBorrowed());

        // حالة فشل (if = True)
        assertThrows(IllegalStateException.class, () -> {
            book.borrow(new User("Bob", "pass", "User"));
        });
    }
    
    // ... (اختبارات Return, toFileString, fromFileString كما هي، فهي جيدة جداً)

    // ============================================
    // 4. اختبار الغرامات والـ ToString (تم دمجها)
    // ============================================
    @Test
    @DisplayName("Test Fine Calculation and ToString")
    void testFineAndString() {
        // اختبار الغرامة الصفرية
        assertEquals(0.0, book.getFineAmount()); // يغطي المسار دون تأخير

        // اختبار الغرامة مع التأخير (يغطي مسار الحساب عبر super.getFineAmount)
        book.borrow(user);
        book.setDueDate(LocalDate.now().minusDays(5)); // تأخير 5 أيام
        assertEquals(50.0, book.getFineAmount(), 0.01); // 5 * 10.0 (الافتراضي)

        // اختبار toString
        String str = book.toString();
        assertTrue(str.contains("Clean Code"));
        assertTrue(str.contains("Robert C. Martin"));
    }
    // ============================================
    // 5. اختبار toFileString (تغطية الـ Ternary Operators)
    // ============================================
    @Test
    @DisplayName("Test toFileString scenarios")
    void testToFileString() {
        // السيناريو A: كتاب غير مستعار (يجب أن يطبع "null" للتواريخ واليوزر)
        String availableStr = book.toFileString();
        assertTrue(availableStr.contains("null;null"), "Should contain nulls for empty fields");

        // السيناريو B: كتاب مستعار (يجب أن يطبع القيم الحقيقية)
        book.borrow(user);
        String borrowedStr = book.toFileString();
        assertFalse(borrowedStr.contains("null"), "Should NOT contain nulls for borrowed book");
        assertTrue(borrowedStr.contains(user.getName()));
    }

    // ============================================
    // 6. اختبار fromFileString (أهم جزء للكفرج العالي)
    // ============================================
    @Test
    @DisplayName("Test fromFileString with all branches")
    void testFromFileString() {
        // 1. اختبار القيم الفارغة (Null/Empty guards)
        assertNull(Book.fromFileString(null));
        assertNull(Book.fromFileString(""));
        assertNull(Book.fromFileString("   "));

        // 2. اختبار السلسلة القصيرة (Validation check)
        assertNull(Book.fromFileString("BOOK;Short")); 

        // 3. اختبار مع كلمة BOOK (Offset = 1)
        String standardLine = "BOOK;Title;Author;ISBN;true;2023-01-01;User;true";
        Book b1 = Book.fromFileString(standardLine);
        assertNotNull(b1);
        assertEquals("Title", b1.getTitle());
        assertTrue(b1.isBorrowed());
        assertTrue(b1.isFineIssued());

        // 4. اختبار بدون كلمة BOOK (Offset = 0) - هذا يغطي الـ else في الكود
        String noTagLine = "Title2;Author2;ISBN2;false;null;null;false";
        Book b2 = Book.fromFileString(noTagLine);
        assertNotNull(b2);
        assertEquals("Title2", b2.getTitle());
        assertFalse(b2.isBorrowed());

        // 5. اختبار الـ Parsing للقيم "null" (تغطية شروط if inside parsing)
        // هذا السطر يختبر الفروع التي تتجاهل الـ parsing إذا كانت القيمة "null"
        String nullsLine = "BOOK;T;A;I;true;null;null;false";
        Book b3 = Book.fromFileString(nullsLine);
        assertNotNull(b3);
        assertTrue(b3.isBorrowed()); 
        assertNull(b3.getDueDate()); // تأكدنا أنه لم يحاول عمل parse لـ "null"
        assertNull(b3.getBorrowedBy());
    }
}