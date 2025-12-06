package servicetest;

import model.Book;
import model.CD;
import model.User;
import model.media;
import service.AdminService;
import service.BookService;
import service.UserService;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BookServiceTest {

    private BookService bookService;
    private AdminService stubAdminService;
    private UserService stubUserService;
    
    private final String TEST_FILE = "src/main/resources/books_test.txt";
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final InputStream originalIn = System.in;

    @BeforeEach
    void setUp() throws IOException {
        // 1. إنشاء ملف وهمي فارغ
        createDummyFile();
        
        // 2. توجيه المخرجات
        System.setOut(new PrintStream(outContent));

        // 3. إنشاء خدمات وهمية (Stubs) للتحكم في سلوك الاختبار
        
        // AdminService: دائماً logged in
        stubAdminService = new AdminService() {
            @Override
            public boolean isLoggedIn() { return true; }
        };

        // UserService: يرجع مستخدمين وهميين عند الطلب
        stubUserService = new UserService("dummy_users.txt") {
            @Override
            public User findUserByName(String name) {
                if ("BlockedUser".equals(name)) {
                    User u = new User("BlockedUser", "pass", "User");
                    u.setOutstandingFine(50.0);
                    return u;
                }
                return new User(name, "pass", "User");
            }
        };

        // 4. تهيئة الخدمة المراد اختبارها
        bookService = new BookService(stubAdminService, stubUserService, TEST_FILE);
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
        System.setIn(originalIn);
        File f = new File(TEST_FILE);
        if (f.exists()) f.delete();
    }

    private void createDummyFile() throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(TEST_FILE))) {
            // إضافة كتاب و CD بشكل افتراضي للاختبار
            pw.println("BOOK;Java Book;Author A;111;false;null;null;false");
            pw.println("CD;Music CD;Artist B;222;false;null;null;false");
        }
    }

    // =========================================================
    // 1. Load & Save Logic
    // =========================================================
    @Test
    @DisplayName("Load Items: Books and CDs loaded correctly")
    void testLoadItems() {
        List<media> items = bookService.getAllBooks();
        assertEquals(2, items.size());
        
        assertTrue(items.get(0) instanceof Book);
        assertEquals("Java Book", items.get(0).getTitle());
        
        assertTrue(items.get(1) instanceof CD);
        assertEquals("Music CD", items.get(1).getTitle());
    }

    // =========================================================
    // 2. Add Item Logic
    // =========================================================
    @Test
    @DisplayName("Add Book: Success")
    void testAddBookSuccess() {
        bookService.addBook("New Book", "New Auth", "333");
        assertNotNull(bookService.findMediaById("333"));
        assertTrue(outContent.toString().contains("Book added successfully"));
    }

    @Test
    @DisplayName("Add CD: Success")
    void testAddCDSuccess() {
        bookService.addCD("New CD", "New Artist", "444");
        assertNotNull(bookService.findMediaById("444"));
        assertTrue(outContent.toString().contains("CD added successfully"));
    }

    @Test
    @DisplayName("Add Item: Fail (Duplicate ID)")
    void testAddDuplicate() {
        bookService.addBook("Dup", "Auth", "111"); // 111 exists
        assertTrue(outContent.toString().contains("already exists"));
    }
    
    @Test
    @DisplayName("Add Item: Fail (Not Admin)")
    void testAddNotAdmin() {
        // نغير الـ Stub ليرجع false
        AdminService notLoggedIn = new AdminService() {
            @Override
            public boolean isLoggedIn() { return false; }
        };
        BookService limitedService = new BookService(notLoggedIn, stubUserService, TEST_FILE);
        
        limitedService.addBook("Fail", "Fail", "999");
        assertTrue(outContent.toString().contains("Access denied"));
    }

    // =========================================================
    // 3. Search Logic
    // =========================================================
    @Test
    @DisplayName("Search: Found and Not Found")
    void testSearch() {
        // بحث ناجح
        bookService.searchBook("Java");
        assertTrue(outContent.toString().contains("Search results"));
        
        // بحث فاشل
        bookService.searchBook("Python");
        assertTrue(outContent.toString().contains("No items found"));
    }

    // =========================================================
    // 4. Borrow Logic
    // =========================================================
    @Test
    @DisplayName("Borrow: Success")
    void testBorrowSuccess() {
        User u = new User("John", "pass", "User");
        boolean result = bookService.borrowBook(u, "111"); // الكتاب المتاح
        
        assertTrue(result);
        media item = bookService.findMediaById("111");
        assertTrue(item.isBorrowed());
        assertEquals("John", item.getBorrowedBy().getName());
    }

    @Test
    @DisplayName("Borrow: Fail (User has Fines)")
    void testBorrowFailFine() {
        User blocked = new User("BlockedUser", "pass", "User");
        boolean result = bookService.borrowBook(blocked, "111");
        
        assertFalse(result);
        assertTrue(outContent.toString().contains("cannot borrow new items until you pay"));
    }

    @Test
    @DisplayName("Borrow: Fail (Already Borrowed)")
    void testBorrowFailAlreadyBorrowed() {
        User u = new User("John", "pass", "User");
        bookService.borrowBook(u, "111"); // Borrow first time
        
        boolean result = bookService.borrowBook(u, "111"); // Try again
        assertFalse(result);
        assertTrue(outContent.toString().contains("already borrowed"));
    }
    
    @Test
    @DisplayName("Borrow: Fail (Item Not Found)")
    void testBorrowNotFound() {
        User u = new User("John", "pass", "User");
        boolean result = bookService.borrowBook(u, "999");
        assertFalse(result);
    }

    // =========================================================
    // 5. Return Logic (The Complex Part)
    // =========================================================
    @Test
    @DisplayName("Return: Normal Return (No Fines)")
    void testReturnNormal() {
        User u = new User("John", "pass", "User");
        bookService.borrowBook(u, "111");
        
        bookService.returnBook("111", u);
        
        media item = bookService.findMediaById("111");
        assertFalse(item.isBorrowed());
        assertTrue(outContent.toString().contains("Item returned successfully"));
    }

    @Test
    @DisplayName("Return: Overdue Item - Pay Now (Yes)")
    void testReturnOverduePayYes() {
        User u = new User("John", "pass", "User");
        bookService.borrowBook(u, "111");
        
        // جعل الكتاب متأخراً 5 أيام
        bookService.makeBookOverdue("111", 5);

        // محاكاة إدخال "yes" للدفع
        System.setIn(new ByteArrayInputStream("yes\n".getBytes()));

        bookService.returnBook("111", u);

        media item = bookService.findMediaById("111");
        assertFalse(item.isBorrowed());
        assertTrue(outContent.toString().contains("Payment Successful"));
    }

    @Test
    @DisplayName("Return: Overdue Item - Pay Now (No)")
    void testReturnOverduePayNo() {
        User u = new User("John", "pass", "User");
        bookService.borrowBook(u, "111");
        bookService.makeBookOverdue("111", 5);

        // محاكاة إدخال "no" لرفض الدفع
        System.setIn(new ByteArrayInputStream("no\n".getBytes()));

        bookService.returnBook("111", u);

        media item = bookService.findMediaById("111");
        assertTrue(item.isBorrowed()); // لا يزال مستعاراً
        assertTrue(outContent.toString().contains("Return cancelled"));
    }

    @Test
    @DisplayName("Return: Fail Scenarios")
    void testReturnFails() {
        User u = new User("John", "pass", "User");
        User other = new User("Other", "pass", "User");

        // 1. Item not found
        bookService.returnBook("999", u);
        assertTrue(outContent.toString().contains("Item not found"));

        // 2. Not borrowed
        bookService.returnBook("222", u); // CD is available
        assertTrue(outContent.toString().contains("already returned"));

        // 3. Wrong user
        bookService.borrowBook(u, "111");
        bookService.returnBook("111", other); // Other tries to return John's book
        assertTrue(outContent.toString().contains("cannot return an item borrowed by another user"));
    }
    
    // =========================================================
    // 6. Test makeBookOverdue (Helper)
    // =========================================================
    @Test
    void testMakeOverdueHelper() {
        bookService.makeBookOverdue("999", 5); // Not found
        assertTrue(outContent.toString().contains("Item not found"));
    }
}