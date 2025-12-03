import model.*;


import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BookTest {

    private Book book;
    private User user;

    @BeforeEach
    void setUp() {
        // تجهيز كائن كتاب ومستخدم قبل كل اختبار
        book = new Book("Clean Code", "Robert Martin", "978-0132350884");
        user = new User("Ahmad", "1234", "User");
    }

    // ========================================================
    // 1️⃣ اختبار البيانات الأساسية (Getters & Initialization)
    // ========================================================
    @Test
    void testBookInitialization() {
        assertEquals("Clean Code", book.getTitle());
        assertEquals("Robert Martin", book.getAuthor()); // getCreator alias
        assertEquals("978-0132350884", book.getIsbn()); // getId alias
        
        // القيم الافتراضية
        assertFalse(book.isBorrowed());
        assertNull(book.getBorrowedBy());
        assertNull(book.getDueDate());
    }

    @Test
    void testLoanPeriodAndFine() {
        // التأكد من تطبيق القواعد الخاصة بالكتاب (Sprint 2 & 5)
        assertEquals(28, book.getLoanPeriod(), "Book loan period should be 28 days");
        assertEquals(10.0, book.getDailyFine(), "Book daily fine should be 10.0");
    }

    // ========================================================
    // 2️⃣ اختبار عملية الاستعارة (Borrowing Logic)
    // ========================================================
    @Test
    void testBorrowBook_Success() {
        book.borrow(user);

        assertTrue(book.isBorrowed());
        assertEquals(user, book.getBorrowedBy());
        
        // التحقق من تاريخ الإرجاع (اليوم + 28 يوم)
        LocalDate expectedDate = LocalDate.now().plusDays(28);
        assertEquals(expectedDate, book.getDueDate());
    }

    @Test
    void testBorrowBook_Fail_IfAlreadyBorrowed() {
        // الاستعارة الأولى (ناجحة)
        book.borrow(user);

        // الاستعارة الثانية (يجب أن تفشل وترمي خطأ)
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            book.borrow(new User("Samer", "000", "User"));
        });

        assertEquals("Book is already borrowed!", exception.getMessage());
    }

    // ========================================================
    // 3️⃣ اختبار عملية الإرجاع (Returning Logic)
    // ========================================================
    @Test
    void testReturnBook() {
        // استعارة ثم إرجاع
        book.borrow(user);
        book.returnBook();

        assertFalse(book.isBorrowed());
        assertNull(book.getBorrowedBy());
        assertNull(book.getDueDate());
        assertFalse(book.isFineIssued());
    }

    // ========================================================
    // 4️⃣ اختبار التعامل مع الملفات (String Parsing)
    // ========================================================
    @Test
    void testToFileString() {
        // حالة الكتاب غير مستعار
        String expected = "BOOK;Clean Code;Robert Martin;978-0132350884;false;null;null;false";
        assertEquals(expected, book.toFileString());

        // حالة الكتاب مستعار
        book.borrow(user);
        String fileString = book.toFileString();
        
        // نتأكد أن النص يحتوي على كلمة true (مستعار) واسم المستعير وتاريخ
        assertTrue(fileString.contains("true"));
        assertTrue(fileString.contains("Ahmad"));
        assertTrue(fileString.contains(LocalDate.now().plusDays(28).toString()));
    }

    @Test
    void testFromFileString() {
        // محاكاة سطر من ملف books.txt
        String line = "BOOK;Design Patterns;GoF;111222;true;2025-12-01;Sami;false";
        
        Book loadedBook = Book.fromFileString(line);

        assertNotNull(loadedBook);
        assertEquals("Design Patterns", loadedBook.getTitle());
        assertEquals("GoF", loadedBook.getAuthor());
        assertEquals("111222", loadedBook.getIsbn());
        assertTrue(loadedBook.isBorrowed());
        assertEquals("Sami", loadedBook.getBorrowedBy().getName());
        assertEquals(LocalDate.parse("2025-12-01"), loadedBook.getDueDate());
    }
    
    @Test
    void testFromFileString_InvalidData() {
        assertNull(Book.fromFileString(null));
        assertNull(Book.fromFileString(""));
        assertNull(Book.fromFileString("INVALID;FORMAT")); // بيانات ناقصة
    }
}