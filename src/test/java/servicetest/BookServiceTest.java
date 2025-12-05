package servicetest;

import model.Book;
import model.CD;
import model.User;
import model.media;
import service.AdminService;
import service.BookService;
import service.UserService;

import org.junit.jupiter.api.*;

import java.io.*;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class BookServiceTest {

    private BookService bookService;
    private AdminService stubAdminService;
    private AdminService stubAdminNotLogged;
    private UserService stubUserService;
    private User regularUser;
    private User secondUser;

    private final String TEST_FILE_PATH = "src/main/resources/books_test.txt";
    private final InputStream originalIn = System.in;

    @BeforeEach
    void setUp() {
        regularUser = new User("John", "123", "User");
        secondUser = new User("Mike", "555", "User");

        // Admin logged in
        stubAdminService = new AdminService() {
            @Override
            public boolean isLoggedIn() { return true; }
        };

        // Admin NOT logged in
        stubAdminNotLogged = new AdminService() {
            @Override
            public boolean isLoggedIn() { return false; }
        };

        // User service stub
        stubUserService = new UserService() {
            @Override
            public User findUserByName(String name) {
                if (name.equals("John")) return regularUser;
                if (name.equals("Mike")) return secondUser;
                return null;
            }
        };

        bookService = new BookService(stubAdminService, stubUserService, TEST_FILE_PATH);
        bookService.getAllBooks().clear();
    }

    @AfterEach
    void tearDown() {
        System.setIn(originalIn);
        File f = new File(TEST_FILE_PATH);
        if (f.exists()) f.delete();
    }

    // ---------------------------------------------------------
    // 1. ADD BOOK – Admin allowed
    // ---------------------------------------------------------
    @Test
    void testAddBook_AdminAllowed() {
        bookService.addBook("Java", "Author", "AAA");
        assertNotNull(bookService.findMediaById("AAA"));
    }

    // ---------------------------------------------------------
    // 2. ADD BOOK – Admin NOT logged in
    // ---------------------------------------------------------
    @Test
    void testAddBook_AdminDenied() {
        BookService bs = new BookService(stubAdminNotLogged, stubUserService, TEST_FILE_PATH);
        bs.getAllBooks().clear();
        bs.addBook("Fail", "Auth", "X1");

        assertNull(bs.findMediaById("X1"));  // لم يتم الإضافة
    }

    // ---------------------------------------------------------
    // 3. UNIQUE ID check
    // ---------------------------------------------------------
    @Test
    void testAddDuplicateID() {
        bookService.addBook("A1", "Auth", "ID1");
        bookService.addBook("A2", "Auth", "ID1");

        assertEquals(1, bookService.getAllBooks().size());
    }

    // ---------------------------------------------------------
    // 4. ADD CD
    // ---------------------------------------------------------
    @Test
    void testAddCD() {
        bookService.addCD("Thriller", "MJ", "C100");
        assertTrue(bookService.findMediaById("C100") instanceof CD);
    }

    // ---------------------------------------------------------
    // 5. BORROW – success
    // ---------------------------------------------------------
    @Test
    void testBorrowSuccess() {
        bookService.addBook("B", "A", "123");
        boolean ok = bookService.borrowBook(regularUser, "123");

        assertTrue(ok);
        assertTrue(bookService.findMediaById("123").isBorrowed());
    }

    // ---------------------------------------------------------
    // 6. BORROW – user has fines
    // ---------------------------------------------------------
    @Test
    void testBorrowFailsDueToFine() {
        bookService.addBook("B", "A", "F1");
        regularUser.setOutstandingFine(20);

        boolean ok = bookService.borrowBook(regularUser, "F1");
        assertFalse(ok);
    }

    // ---------------------------------------------------------
    // 7. BORROW – item not found
    // ---------------------------------------------------------
    @Test
    void testBorrowItemNotFound() {
        boolean ok = bookService.borrowBook(regularUser, "XXX");
        assertFalse(ok);
    }

    // ---------------------------------------------------------
    // 8. BORROW – item already borrowed
    // ---------------------------------------------------------
    @Test
    void testBorrowAlreadyBorrowed() {
        bookService.addBook("B", "A", "BB1");
        bookService.borrowBook(regularUser, "BB1");

        boolean ok = bookService.borrowBook(regularUser, "BB1");
        assertFalse(ok);
    }

    // ---------------------------------------------------------
    // 9. RETURN – normal successful return
    // ---------------------------------------------------------
    @Test
    void testReturnNormal() {
        bookService.addBook("R", "A", "R1");
        bookService.borrowBook(regularUser, "R1");

        bookService.returnBook("R1", regularUser);

        assertFalse(bookService.findMediaById("R1").isBorrowed());
    }

    // ---------------------------------------------------------
    // 10. RETURN – item NOT found
    // ---------------------------------------------------------
    @Test
    void testReturnItemNotFound() {
        bookService.returnBook("MISSING", regularUser);
        // Nothing to assert except no crash
    }

    // ---------------------------------------------------------
    // 11. RETURN – item NOT borrowed
    // ---------------------------------------------------------
    @Test
    void testReturnNotBorrowed() {
        bookService.addBook("X", "Y", "NB1");
        bookService.returnBook("NB1", regularUser);
        assertFalse(bookService.findMediaById("NB1").isBorrowed());
    }

    // ---------------------------------------------------------
    // 12. RETURN – item borrowed by ANOTHER user
    // ---------------------------------------------------------
    @Test
    void testReturnBorrowedByAnotherUser() {
        bookService.addBook("Z", "T", "DIFF");
        bookService.borrowBook(regularUser, "DIFF");

        bookService.returnBook("DIFF", secondUser);

        assertTrue(bookService.findMediaById("DIFF").isBorrowed());
    }

    // ---------------------------------------------------------
    // 13. RETURN – Overdue + Pay YES
    // ---------------------------------------------------------
    @Test
    void testReturnOverdue_PayYes() {
        bookService.addBook("Late", "A", "OV1");
        bookService.borrowBook(regularUser, "OV1");
        bookService.makeBookOverdue("OV1", 4);

        System.setIn(new ByteArrayInputStream("yes\n".getBytes()));

        bookService.returnBook("OV1", regularUser);

        assertFalse(bookService.findMediaById("OV1").isBorrowed());
    }

    // ---------------------------------------------------------
    // 14. RETURN – Overdue + Pay NO
    // ---------------------------------------------------------
    @Test
    void testReturnOverdue_PayNo() {
        bookService.addBook("Late2", "A", "OV2");
        bookService.borrowBook(regularUser, "OV2");
        bookService.makeBookOverdue("OV2", 5);

        System.setIn(new ByteArrayInputStream("no\n".getBytes()));

        bookService.returnBook("OV2", regularUser);

        assertTrue(bookService.findMediaById("OV2").isBorrowed());
    }

    // ---------------------------------------------------------
    // 15. Search simple test
    // ---------------------------------------------------------
    @Test
    void testSearch() {
        bookService.addBook("Java Basics", "Auth", "JB1");
        assertNotNull(bookService.findMediaById("JB1"));
    }

    // ---------------------------------------------------------
    // 16. makeBookOverdue – item not borrowed
    // ---------------------------------------------------------
    @Test
    void testMakeBookOverdue_notBorrowed() {
        bookService.addBook("No borrow", "A", "NOB");
        bookService.makeBookOverdue("NOB", 3);

        assertNull(bookService.findMediaById("NOB").getDueDate());
    }
}
