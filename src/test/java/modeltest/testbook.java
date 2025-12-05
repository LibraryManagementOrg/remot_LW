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
        // تهيئة البيانات قبل كل اختبار
        user = new User("Alice", "password123", "User");
        book = new Book("Clean Code", "Robert C. Martin", "978-0132350884");
    }

    @Test
    @DisplayName("1. Test Constructor & Inheritance (Media fields)")
    void testConstructorAndInheritance() {
        // التأكد من أن البيانات انتقلت للكلاس الأب (media) بشكل صحيح
        assertEquals("Clean Code", book.getTitle());
        assertEquals("Robert C. Martin", book.getCreator()); // في media اسمه creator
        assertEquals("978-0132350884", book.getId());       // في media اسمه id
        
        // التأكد من القيم الافتراضية
        assertFalse(book.isBorrowed());
        assertNull(book.getDueDate());
        assertNull(book.getBorrowedBy());
    }

    @Test
    @DisplayName("2. Test Abstract Methods Implementation")
    void testAbstractMethods() {
        // التأكد من أن Book نفذ الدوال المجردة بالشكل المطلوب
        assertEquals(28, book.getLoanPeriod(), "Loan period for books should be 28 days");
        assertTrue(book.getDailyFine() > 0, "Daily fine should be positive");
    }

    @Test
    @DisplayName("3. Test Borrow Logic")
    void testBorrowLogic() {
        // تنفيذ الاستعارة
        book.borrow(user);

        // التحقق من تحديث حقول الـ media
        assertTrue(book.isBorrowed());
        assertEquals(user, book.getBorrowedBy());
        
        // التحقق من تاريخ الاستحقاق
        LocalDate expectedDate = LocalDate.now().plusDays(28);
        assertEquals(expectedDate, book.getDueDate());
    }

    @Test
    @DisplayName("4. Test Return Logic")
    void testReturnLogic() {
        // ترتيب: استعارة الكتاب أولاً
        book.borrow(user);
        
        // فعل: إرجاع الكتاب
        book.returnBook();

        // تحقق: تصفير البيانات
        assertFalse(book.isBorrowed());
        assertNull(book.getDueDate());
        assertNull(book.getBorrowedBy());
        assertFalse(book.isFineIssued());
    }

    @Test
    @DisplayName("5. Test Overdue and Fine Calculation")
    void testFineCalculation() {
        // 1. استعارة الكتاب
        book.borrow(user);
        
        // 2. تلاعب بالتاريخ (نجعل تاريخ الاستحقاق قبل 5 أيام من اليوم)
        LocalDate pastDate = LocalDate.now().minusDays(5);
        book.setDueDate(pastDate);

        // 3. التحقق من أن الكتاب يعتبر متأخراً
        assertTrue(book.isOverdue(), "Book should be overdue");

        // 4. التحقق من حساب الغرامة (5 أيام * الغرامة اليومية)
        double expectedFine = 5 * book.getDailyFine();
        assertEquals(expectedFine, book.getFineAmount(), 0.01, "Fine calculation should be correct");
    }
    
    @Test
    @DisplayName("6. Test Fine Amount is Zero if Not Overdue")
    void testFineZeroIfNotOverdue() {
        book.borrow(user);
        // تاريخ الاستحقاق في المستقبل (الوضع الطبيعي)
        assertFalse(book.isOverdue());
        assertEquals(0.0, book.getFineAmount());
    }

    @Test
    @DisplayName("7. Test File String Generation (toFileString)")
    void testToFileString() {
        // تنسيق الكتاب المتوقع عند الحفظ
        String result = book.toFileString();
        
        assertTrue(result.startsWith("BOOK;"), "Should start with BOOK tag");
        assertTrue(result.contains("Clean Code"), "Should contain title");
        assertTrue(result.contains("false"), "Should contain isBorrowed status");
    }

    @Test
    @DisplayName("8. Test Parsing from File (fromFileString)")
    void testFromFileString() {
        // محاكاة سطر من ملف books.txt
        String line = "BOOK;Design Patterns;GoF;12345;true;2023-01-01;Bob;true";
        
        Book parsedBook = Book.fromFileString(line);

        assertNotNull(parsedBook);
        assertEquals("Design Patterns", parsedBook.getTitle());
        assertEquals("GoF", parsedBook.getCreator());
        assertEquals("12345", parsedBook.getId());
        assertTrue(parsedBook.isBorrowed());
        // في دالة fromFileString يتم تعيين الغرامة إذا كانت true في الملف
        assertTrue(parsedBook.isFineIssued()); 
        assertEquals("Bob", parsedBook.getBorrowedBy().getName());
    }
}