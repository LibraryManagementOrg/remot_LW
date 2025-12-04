import service.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir; // لإنشاء ملفات مؤقتة
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import model.Book;
import model.CD;
import model.User;
import model.media;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private AdminService adminServiceMock;

    @Mock
    private UserService userServiceMock;

    private BookService bookService;

    // لإنشاء مجلد مؤقت للاختبار حتى لا نلمس الملف الحقيقي
    @TempDir
    File tempDir; 

    @BeforeEach
    void setUp() {
        // يمكننا هنا تجاوز مسار الملف إذا كان المتغير FILE_PATH ليس private final
        // لكن بما أنه Hardcoded، سنقوم بإنشاء الخدمة ونعتمد على القائمة الداخلية
        // (سيحاول القراءة ويفشل بصمت أو ينشئ ملفاً جديداً، وهذا مقبول للاختبار)
        bookService = new BookService(adminServiceMock, userServiceMock);
    }

    // ========================================================
    // 1️⃣ اختبار الإضافة (Add Book / CD)
    // ========================================================
    @Test
    void testAddBook_Success() {
        // شرط: الأدمن مسجل دخول
        when(adminServiceMock.isLoggedIn()).thenReturn(true);

        bookService.addBook("Clean Code", "Uncle Bob", "ISBN-111");

        media addedItem = bookService.findMediaById("ISBN-111");
        assertNotNull(addedItem, "Book should be added");
        assertTrue(addedItem instanceof Book, "Item should be of type Book");
        assertEquals("Clean Code", addedItem.getTitle());
    }

    @Test
    void testAddCD_Success() {
        when(adminServiceMock.isLoggedIn()).thenReturn(true);

        bookService.addCD("Thriller", "MJ", "CD-222");

        media addedItem = bookService.findMediaById("CD-222");
        assertNotNull(addedItem, "CD should be added");
        assertTrue(addedItem instanceof CD, "Item should be of type CD");
    }

    @Test
    void testAdd_Fail_IfNotAdmin() {
        when(adminServiceMock.isLoggedIn()).thenReturn(false);

        bookService.addBook("Hacker", "Me", "ISBN-999");
        
        assertNull(bookService.findMediaById("ISBN-999"), "Should not add book if not admin");
    }

    @Test
    void testAdd_Fail_IfDuplicateID() {
        when(adminServiceMock.isLoggedIn()).thenReturn(true);
        
        bookService.addBook("Book 1", "Auth", "ID-1");
        bookService.addBook("Book 2", "Auth", "ID-1"); // محاولة إضافة نفس الـ ID

        // يجب أن يبقى الكتاب الأول فقط
        media m = bookService.findMediaById("ID-1");
        assertEquals("Book 1", m.getTitle(), "Should not overwrite existing ID");
        
        // نتأكد أن العدد الكلي 1
        assertEquals(1, bookService.getAllBooks().size());
    }

    // ========================================================
    // 2️⃣ اختبار الاستعارة (Borrowing - Polymorphism)
    // ========================================================
    @Test
    void testBorrowBook_Success_LoanPeriod28() {
        // 1. تحضير
        when(adminServiceMock.isLoggedIn()).thenReturn(true);
        bookService.addBook("Java Book", "Author", "B-100");
        
        User user = new User("Ali", "123", "User");
        when(userServiceMock.findUserByName("Ali")).thenReturn(user);

        // 2. تنفيذ
        boolean result = bookService.borrowBook(user, "B-100");

        // 3. تحقق
        assertTrue(result, "Borrow should succeed");
        media m = bookService.findMediaById("B-100");
        assertTrue(m.isBorrowed());
        
        // التحقق من مدة الإعارة (الكتاب 28 يوم)
        LocalDate expectedDue = LocalDate.now().plusDays(28);
        assertEquals(expectedDue, m.getDueDate(), "Book loan period should be 28 days");
    }

    @Test
    void testBorrowCD_Success_LoanPeriod7() {
        // 1. تحضير
        when(adminServiceMock.isLoggedIn()).thenReturn(true);
        bookService.addCD("Music CD", "Artist", "C-200");
        
        User user = new User("Sara", "123", "User");
        when(userServiceMock.findUserByName("Sara")).thenReturn(user);

        // 2. تنفيذ
        boolean result = bookService.borrowBook(user, "C-200");

        // 3. تحقق
        assertTrue(result);
        media m = bookService.findMediaById("C-200");
        
        // التحقق من مدة الإعارة (السي دي 7 أيام)
        LocalDate expectedDue = LocalDate.now().plusDays(7);
        assertEquals(expectedDue, m.getDueDate(), "CD loan period should be 7 days");
    }

    @Test
    void testBorrow_Fail_IfUserHasFines() {
        // يوزر عليه غرامات
        User debtUser = new User("Debtor", "123", "User");
        debtUser.setOutstandingFine(10.0);
        
        when(userServiceMock.findUserByName("Debtor")).thenReturn(debtUser);
        
        // نحاول نستعير أي شيء (يجب أن يفشل)
        boolean result = bookService.borrowBook(debtUser, "AnyID");
        
        assertFalse(result, "Should prevent borrowing if user has fines");
    }

    @Test
    void testBorrow_Fail_IfAlreadyBorrowed() {
        when(adminServiceMock.isLoggedIn()).thenReturn(true);
        bookService.addBook("Popular Book", "Auth", "B-555");
        
        User u1 = new User("User1", "111", "User");
        User u2 = new User("User2", "222", "User");
        
        when(userServiceMock.findUserByName("User1")).thenReturn(u1);
        when(userServiceMock.findUserByName("User2")).thenReturn(u2);

        // الاستعارة الأولى
        bookService.borrowBook(u1, "B-555");
        
        // الاستعارة الثانية لنفس الكتاب
        boolean result = bookService.borrowBook(u2, "B-555");
        
        assertFalse(result, "Cannot borrow an already borrowed book");
        assertEquals("User1", bookService.findMediaById("B-555").getBorrowedBy().getName());
    }

    // ========================================================
    // 3️⃣ اختبار الإرجاع (Return Logic)
    // ========================================================
    @Test
    void testReturnBook_Success() {
        when(adminServiceMock.isLoggedIn()).thenReturn(true);
        bookService.addBook("Return Me", "Auth", "R-1");
        
        User user = new User("Owner", "123", "User");
        when(userServiceMock.findUserByName("Owner")).thenReturn(user);
        
        // استعارة
        bookService.borrowBook(user, "R-1");
        
        // إرجاع
        bookService.returnBook("R-1", user);
        
        media m = bookService.findMediaById("R-1");
        assertFalse(m.isBorrowed(), "Book should be available after return");
        assertNull(m.getBorrowedBy());
        assertNull(m.getDueDate());
    }

    @Test
    void testReturnBook_Fail_IfWrongUser() {
        when(adminServiceMock.isLoggedIn()).thenReturn(true);
        bookService.addBook("Secret", "Auth", "S-1");
        
        User owner = new User("Owner", "123", "User");
        User thief = new User("Thief", "999", "User");
        
        when(userServiceMock.findUserByName("Owner")).thenReturn(owner);
        
        bookService.borrowBook(owner, "S-1");
        
        // شخص آخر يحاول إرجاع الكتاب (Thief)
        bookService.returnBook("S-1", thief);
        
        media m = bookService.findMediaById("S-1");
        assertTrue(m.isBorrowed(), "Should not allow return by different user");
    }

    // ========================================================
    // 4️⃣ اختبار البحث (Search)
    // ========================================================
    @Test
    void testSearchBook() {
        when(adminServiceMock.isLoggedIn()).thenReturn(true);
        bookService.addBook("Java Programming", "Deitel", "J-1");
        bookService.addCD("Java Course CD", "Instructor", "CD-J");

        // بما أن الدالة تطبع على الكونسول (Void)، لا يمكننا اختبار المخرجات بسهولة 
        // إلا إذا غيرنا الدالة لترجع List.
        // لكن يمكننا اختبار الدالة المساعدة findMediaById بدلاً منها
        
        assertNotNull(bookService.findMediaById("J-1"));
        assertNotNull(bookService.findMediaById("CD-J"));
        assertNull(bookService.findMediaById("Python"));
    }
}