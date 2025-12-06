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
import java.util.ArrayList;
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
        createDummyFile();
        
        System.setOut(new PrintStream(outContent));

        stubAdminService = new AdminService() {
            @Override
            public boolean isLoggedIn() { return true; }
        };

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
            // Mocking for findUserByName in tests
        };

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
    // 2. Add Item Logic (Using String Return)
    // =========================================================
    @Test
    @DisplayName("Add Book: Success")
    void testAddBookSuccess() {
        String result = bookService.addBook("New Book", "New Auth", "333");
        assertNotNull(bookService.findMediaById("333"));
        assertTrue(result.contains("added successfully"));
    }

    @Test
    @DisplayName("Add CD: Success")
    void testAddCDSuccess() {
        String result = bookService.addCD("New CD", "New Artist", "444");
        assertNotNull(bookService.findMediaById("444"));
        assertTrue(result.contains("added successfully"));
    }

    @Test
    @DisplayName("Add Item: Fail (Duplicate ID)")
    void testAddDuplicate() {
        String result = bookService.addBook("Dup", "Auth", "111"); 
        assertTrue(result.contains("already exists"));
    }
    
    @Test
    @DisplayName("Add Item: Fail (Not Admin)")
    void testAddNotAdmin() {
        AdminService notLoggedIn = new AdminService() {
            @Override
            public boolean isLoggedIn() { return false; }
        };
        BookService limitedService = new BookService(notLoggedIn, stubUserService, TEST_FILE);
        
        String result = limitedService.addBook("Fail", "Fail", "999");
        assertTrue(result.contains("Access denied"));
    }

    // =========================================================
    // 3. Search Logic - Assuming searchBook still uses System.out
    // =========================================================
    @Test
    @DisplayName("Search: Found and Not Found")
    void testSearch() {
        bookService.searchBook("Java");
        assertTrue(outContent.toString().contains("Search results"));
        
        bookService.searchBook("Python");
        assertTrue(outContent.toString().contains("No items found"));
    }

    // =========================================================
    // 4. Borrow Logic (Using String Return)
    // =========================================================
    @Test
    @DisplayName("Borrow: Success")
    void testBorrowSuccess() {
        User u = new User("John", "pass", "User");
        String result = bookService.borrowBook(u, "111"); 
        
        assertTrue(result.contains("Borrowed:"));
        media item = bookService.findMediaById("111");
        assertTrue(item.isBorrowed());
    }

    @Test
    @DisplayName("Borrow: Fail (User has Fines)")
    void testBorrowFailFine() {
        User blocked = new User("BlockedUser", "pass", "User");
        String result = bookService.borrowBook(blocked, "111");
        
        assertTrue(result.contains("cannot borrow new items until you pay"));
        assertFalse(bookService.findMediaById("111").isBorrowed());
    }

    @Test
    @DisplayName("Borrow: Fail (Already Borrowed)")
    void testBorrowFailAlreadyBorrowed() {
        User u = new User("John", "pass", "User");
        bookService.borrowBook(u, "111");
        
        String result = bookService.borrowBook(u, "111");
        assertTrue(result.contains("already borrowed"));
    }
    
    @Test
    @DisplayName("Borrow: Fail (Item Not Found)")
    void testBorrowNotFound() {
        User u = new User("John", "pass", "User");
        String result = bookService.borrowBook(u, "999");
        assertTrue(result.contains("Item not found"));
    }

    // =========================================================
    // 5. Return Logic (Using String Return)
    // =========================================================
    @Test
    @DisplayName("Return: Normal Return (No Fines)")
    void testReturnNormal() {
        User u = new User("John", "pass", "User");
        bookService.borrowBook(u, "111");
        
        String result = bookService.returnBook("111", u);
        
        media item = bookService.findMediaById("111");
        assertFalse(item.isBorrowed());
        assertTrue(result.contains("returned successfully"));
    }

    @Test
    @DisplayName("Return: Overdue Item - Denied Payment Needed")
    void testReturnOverdueDenied() {
        User u = new User("John", "pass", "User");
        bookService.borrowBook(u, "111");
        bookService.makeBookOverdue("111", 5);

        // دالة returnBook يجب أن تعيد رسالة تفيد بالرفض بسبب الغرامة
        String result = bookService.returnBook("111", u);
        
        assertTrue(result.contains("OVERDUE"), "Should be rejected due to overdue status");
        assertTrue(bookService.findMediaById("111").isBorrowed(), "Item should remain borrowed");
    }


    @Test
    @DisplayName("Return: Fail Scenarios")
    void testReturnFails() {
        User u = new User("John", "pass", "User");
        User other = new User("Other", "pass", "User");

        // 1. Item not found
        String result1 = bookService.returnBook("999", u);
        assertTrue(result1.contains("Item not found"));

        // 2. Not borrowed
        String result2 = bookService.returnBook("222", u);
        assertTrue(result2.contains("already returned"));

        // 3. Wrong user
        bookService.borrowBook(u, "111");
        String result3 = bookService.returnBook("111", other);
        assertTrue(result3.contains("cannot return an item borrowed by another user"));
        
        // مسح آثار الاستعارة لتجنب تداخل الاختبارات
        bookService.returnBook("111", u); 
    }
    
    // =========================================================
    // 6. Test makeBookOverdue (Helper)
    // =========================================================
    @Test
    void testMakeOverdueHelper() {
        bookService.makeBookOverdue("999", 5); // Not found
        assertTrue(outContent.toString().contains("Item not found"));
        
        // يجب أن نغطي مسار النجاح أيضاً
        User u = new User("Test", "pass", "User");
        bookService.borrowBook(u, "111");
        bookService.makeBookOverdue("111", 5);
        assertTrue(outContent.toString().contains("is now overdue"));
    }
}