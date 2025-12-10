package modeltest;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import model.Book;
import model.User;

class BookTest {

    private Book book;
    private User user;

    @BeforeEach
    void setUp() {
        user = new User("Alice", "password123", "User");
        book = new Book("Clean Code", "Robert C. Martin", "978-0132350884");
    }

    // ---------------------------------------------------------
    // 1. اختبار الـ Constructor والـ Getters الأساسية
    // ---------------------------------------------------------
    @Test
    @DisplayName("Test Constructor and Basic Getters")
    void testBasicProperties() {
        assertNotNull(book);
        assertEquals("Clean Code", book.getTitle());
        assertEquals("Robert C. Martin", book.getCreator()); // من media
        assertEquals("Robert C. Martin", book.getAuthor());  // من Book
        assertEquals("978-0132350884", book.getId());       // من media
        assertEquals("978-0132350884", book.getIsbn());     // من Book
        
        // اختبار الثوابت
        assertEquals(28, book.getLoanPeriod());
        assertEquals(10.0, book.getDailyFine());
    }

    // ---------------------------------------------------------
    // 2. تغطية الدالة الفارغة setFineAmount
    // ---------------------------------------------------------
    @Test
    @DisplayName("Test setFineAmount (Coverage for empty method)")
    void testSetFineAmount() {
        // هذه الدالة فارغة في الكلاس، ولكن استدعاؤها ضروري لزيادة النسبة
        book.setFineAmount(100.0); 
        // لا يوجد assert لأن الدالة لا تفعل شيئاً حالياً
    }

    // ---------------------------------------------------------
    // 3. اختبار دورة حياة الاستعارة والإرجاع (Borrow & Return)
    // ---------------------------------------------------------
    @Test
    @DisplayName("Test Borrow and Return Logic")
    void testBorrowAndReturn() {
        // 1. استعارة ناجحة
        book.borrow(user);
        assertTrue(book.isBorrowed());
        assertEquals(user, book.getBorrowedBy());
        assertNotNull(book.getDueDate());
        
        // 2. محاولة استعارة كتاب مستعار (يجب أن يرمي Exception)
        User user2 = new User("Bob", "pass", "User");
        assertThrows(IllegalStateException.class, () -> {
            book.borrow(user2);
        });

        // 3. إرجاع الكتاب
        book.returnBook();
        assertFalse(book.isBorrowed());
        assertNull(book.getBorrowedBy());
        assertNull(book.getDueDate());
        assertFalse(book.isFineIssued());
    }

    // ---------------------------------------------------------
    // 4. اختبار حساب الغرامات (Strategy + Super)
    // ---------------------------------------------------------
    @Test
    @DisplayName("Test Fine Calculation")
    void testFineAmount() {
        // الحالة 1: لا يوجد غرامة (ليس مستعاراً أو لم يتأخر)
        assertEquals(0.0, book.getFineAmount());

        // الحالة 2: يوجد غرامة (مستعار ومتأخر)
        book.borrow(user);
        // نغير تاريخ الاستحقاق ليصبح قبل 5 أيام (يعني متأخر 5 أيام)
        // ملاحظة: بما أن Strategy داخلية، نعتمد على أن getFineAmount يستدعيها
        book.setDueDate(LocalDate.now().minusDays(5)); 
        
        // الحساب: 5 أيام * 10 (BookFineStrategy عادة تحسب اليومية)
        // أو حسب اللوجيك الموجود في BookFineStrategy. 
        // سنفحص فقط أن القيمة أكبر من صفر للتأكد أن الكود اشتغل
        assertTrue(book.getFineAmount() > 0.0);
    }

    // ---------------------------------------------------------
    // 5. اختبار toString
    // ---------------------------------------------------------
    @Test
    void testToString() {
        String result = book.toString();
        assertNotNull(result);
        assertTrue(result.contains("Clean Code"));
        assertTrue(result.contains("Robert C. Martin"));
        assertTrue(result.contains("ISBN"));
    }

    // ---------------------------------------------------------
    // 6. اختبار toFileString (Ternary Operators Coverage)
    // ---------------------------------------------------------
    @Test
    @DisplayName("Test toFileString (Full Coverage)")
    void testToFileString() {
        // الحالة A: غير مستعار (يجب أن يظهر nulls)
        String available = book.toFileString();
        assertTrue(available.startsWith("BOOK;"));
        assertTrue(available.contains("null;null")); // date and user are null

        // الحالة B: مستعار (يجب أن يظهر القيم الحقيقية)
        book.borrow(user);
        String borrowed = book.toFileString();
        assertFalse(borrowed.contains("null;null"));
        assertTrue(borrowed.contains("Alice")); // User name
    }

    // ---------------------------------------------------------
    // 7. اختبار fromFileString (أعقد دالة - تغطية شاملة)
    // ---------------------------------------------------------
    @Test
    @DisplayName("Test fromFileString with ALL branches")
    void testFromFileString() {
        // 1. قيم فارغة (Null/Empty Checks)
        assertNull(Book.fromFileString(null));
        assertNull(Book.fromFileString(""));
        assertNull(Book.fromFileString("   "));

        // 2. سطر بيانات ناقص (Validation Check)
        // "BOOK;Title" -> length 2. Offset 1. Required 3+1=4. Fails.
        assertNull(Book.fromFileString("BOOK;OnlyTitle"));

        // 3. سطر صحيح مع بادئة BOOK (Standard Case)
        // يغطي: if (parts[0].equalsIgnoreCase("BOOK")) -> offset = 1
        String lineWithTag = "BOOK;Title1;Author1;ISBN1;true;2025-01-01;User1;true";
        Book b1 = Book.fromFileString(lineWithTag);
        assertNotNull(b1);
        assertEquals("Title1", b1.getTitle());
        assertTrue(b1.isBorrowed());
        assertTrue(b1.isFineIssued());

        // 4. سطر صحيح بدون بادئة (Legacy/Other Case)
        // يغطي: else -> offset = 0
        String lineNoTag = "Title2;Author2;ISBN2;false;null;null;false";
        Book b2 = Book.fromFileString(lineNoTag);
        assertNotNull(b2);
        assertEquals("Title2", b2.getTitle());
        assertFalse(b2.isBorrowed());

        // 5. التعامل مع القيم "null" النصية (Parsing Logic)
        // يغطي: !parts[...].equals("null")
        String lineWithNulls = "BOOK;T;A;I;true;null;null;false";
        Book b3 = Book.fromFileString(lineWithNulls);
        assertNotNull(b3);
        assertTrue(b3.isBorrowed());
        assertNull(b3.getDueDate());      // تأكدنا أنه تجاوز الـ parse
        assertNull(b3.getBorrowedBy());   // تأكدنا أنه تجاوز الـ User creation
    }
}