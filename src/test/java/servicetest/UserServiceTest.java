package servicetest;

import model.Book;
import model.User;
import model.media;
import service.BookService;
import service.UserService;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    private UserService userService;
    private final String TEST_FILE = "src/main/resources/users_test.txt";
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    void setUp() throws IOException {
        createDummyFile();
        
        System.setOut(new PrintStream(outContent));

        userService = new UserService(TEST_FILE);
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
        
        File f = new File(TEST_FILE);
        if (f.exists()) f.delete();
    }

    private void createDummyFile() throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(TEST_FILE))) {
            pw.println("Alice,123,User,0.0,alice@test.com");
            pw.println("Bob,pass,User,50.0,bob@test.com");
            pw.println("Admin,admin,Admin,0.0,admin@test.com");
            pw.println("ShortLine,Data"); 
            pw.println("BadFine,pass,User,NotANumber,email");
        }
    }
    
    // =========================================================
    // 1. اختبار تحميل البيانات (Loading & Parsing)
    // =========================================================
    @Test
    @DisplayName("Load Users: Valid, Invalid Lines, and Bad Number Format")
    void testLoadUsersRobustness() {
        assertNotNull(userService.findUserByName("Alice"));
        assertNotNull(userService.findUserByName("Bob"));
        
        assertNull(userService.findUserByName("ShortLine"));

        User badFineUser = userService.findUserByName("BadFine");
        assertNotNull(badFineUser);
        assertEquals(0.0, badFineUser.getOutstandingFine());
    }
    
    // اختبار تغطية مسار loadUsersFromFile عندما يفشل التحميل (يجب أن يتم تعديل الكونستركتور للاختبار)
    @Test
    @DisplayName("Load Users: Should catch IO Exception")
    void testLoadUsersIOException() {
        // نستخدم مساراً خاطئاً لإجبار دالة loadUsersFromFile على إلقاء IOException
        UserService badLoader = new UserService("/root/unwritable/users.txt");
        // يجب أن نتحقق من أن رسالة الخطأ ظهرت في الكونسول (من دالة loadUsersFromFile)
        assertTrue(outContent.toString().contains("Error reading users file"));
    }

    // =========================================================
    // 2. اختبار تسجيل الدخول والخروج
    // =========================================================
    @Test
    @DisplayName("Login: Success and Fail Scenarios")
    void testLogin() {
        User u = userService.login("Alice", "123", null);
        assertNotNull(u);
        assertTrue(userService.isLoggedIn());
        
        assertNull(userService.login("Alice", "wrong", null));
        assertNull(userService.login("Ghost", "123", null));
    }

    @Test
    @DisplayName("Logout Logic")
    void testLogout() {
        userService.login("Alice", "123", null);
        String result = userService.logout(); // ✅ استقبال String
        assertFalse(userService.isLoggedIn());
        assertNull(userService.getLoggedInUser());
        assertTrue(result.contains("logged out successfully"));
        
        // مسار الخروج عندما لا يكون مسجلاً
        result = userService.logout();
        assertTrue(result.contains("You were not logged in"));
    }
    
    @Test
    @DisplayName("Login triggers fine check if BookService provided")
    void testLoginTriggersFineCheck() {
        BookService stubBookService = new BookService(null, null, "dummy.txt") {
            @Override
            public void saveBooksToFile() { }
            @Override
            public List<media> getAllBooks() { return new ArrayList<>(); }
        };

        userService.login("Alice", "123", stubBookService);
        assertTrue(userService.isLoggedIn());
    }

    // =========================================================
    // 3. اختبار دفع الغرامات (Pay Fine)
    // =========================================================
    @Test
    @DisplayName("Pay Fine: Validation Checks (All return String)")
    void testPayFineValidation() {
        User bob = userService.findUserByName("Bob");

        // 1. محاولة الدفع بدون تسجيل دخول
        userService.logout();
        String result1 = userService.payFine(bob, 10, null); // ✅ استقبال String
        assertTrue(result1.contains("Access denied"));

        // تسجيل دخول بوب
        userService.login("Bob", "pass", null);
        
        // 2. مبلغ سالب
        String result2 = userService.payFine(bob, -5, null);
        assertTrue(result2.contains("Invalid amount"));

        // 3. مبلغ أكبر من الغرامة
        String result3 = userService.payFine(bob, 100, null);
        assertTrue(result3.contains("Error: You entered"));
    }

    @Test
    @DisplayName("Pay Fine: Full Payment & Auto-Return Books")
    void testPayFineSuccessWithAutoReturn() {
        User bob = userService.findUserByName("Bob");
        double initialFine = bob.getOutstandingFine();
        userService.login("Bob", "pass", null);

        Book overdueBook = new Book("Java", "Auth", "111");
        overdueBook.setBorrowed(true);
        overdueBook.setBorrowedBy(bob);
        overdueBook.setDueDate(LocalDate.now().minusDays(5));
        overdueBook.setFineIssued(true);

        BookService stubBookService = new BookService(null, null, "dummy.txt") {
            private boolean saved = false;
            @Override
            public List<media> getAllBooks() {
                List<media> list = new ArrayList<>();
                list.add(overdueBook);
                return list;
            }
            @Override
            public void saveBooksToFile() { this.saved = true; } // mock save
        };

        String result = userService.payFine(bob, initialFine, stubBookService); // دفع كامل
        
        // التحقق:
        assertEquals(0.0, bob.getOutstandingFine());
        assertFalse(overdueBook.isBorrowed());
        assertTrue(result.contains("Payment successful"));
    }
    
    // اختبار دفع جزئي
    @Test
    @DisplayName("Pay Fine: Partial Payment")
    void testPayFinePartial() {
        User bob = userService.findUserByName("Bob");
        userService.login("Bob", "pass", null);
        
        String result = userService.payFine(bob, 10.0, null);
        assertEquals(40.0, bob.getOutstandingFine());
        assertTrue(result.contains("Remaining balance: 40.00"));
    }


    // =========================================================
    // 4. اختبار حساب الغرامات (Check and Apply Fines)
    // =========================================================
    @Test
    @DisplayName("Apply Fines for Overdue Books")
    void testCheckAndApplyFines() {
        User alice = userService.findUserByName("Alice");
        
        Book lateBook = new Book("Late Book", "Auth", "999");
        lateBook.borrow(alice);
        lateBook.setDueDate(LocalDate.now().minusDays(2));
        lateBook.setFineAmount(10.0); // تعيين غرامة يومية افتراضية 

        BookService stubBookService = new BookService(null, null, "dummy.txt") {
            @Override
            public List<media> getAllBooks() {
                return Collections.singletonList(lateBook);
            }
            @Override
            public void saveBooksToFile() { /* Mock save */ }
        };

        userService.checkAndApplyFinesForAllUsers(stubBookService);

        assertEquals(20.0, alice.getOutstandingFine());
        assertTrue(lateBook.isFineIssued());
    }

    @Test
    @DisplayName("Apply Fines: Skip non-overdue or non-borrowed items")
    void testCheckAndApplyFinesSkip() {
        User alice = userService.findUserByName("Alice");
        
        // 1. كتاب متاح
        Book available = new Book("Available", "Auth", "111");
        
        // 2. كتاب مستعار لكن غير متأخر
        Book notLate = new Book("Not Late", "Auth", "222");
        notLate.borrow(alice);
        notLate.setDueDate(LocalDate.now().plusDays(5));

        BookService stubBookService = new BookService(null, null, "dummy.txt") {
            @Override
            public List<media> getAllBooks() {
                List<media> list = new ArrayList<>();
                list.add(available);
                list.add(notLate);
                return list;
            }
        };
        
        userService.checkAndApplyFinesForAllUsers(stubBookService);
        assertEquals(0.0, alice.getOutstandingFine());
    }


    // =========================================================
    // 5. اختبار الحذف والإضافة (Crud Operations)
    // =========================================================
    @Test
    @DisplayName("Delete User: Success and Fail (Returns String)")
    void testDeleteUser() {
        // نجاح
        String result1 = userService.deleteUser("Alice");
        assertTrue(result1.contains("deleted."));
        assertNull(userService.findUserByName("Alice"));

        // فشل (غير موجود)
        String result2 = userService.deleteUser("Ghost");
        assertTrue(result2.contains("not found"));
    }

    @Test
    @DisplayName("Add Fine Helper")
    void testAddFine() {
        User u = userService.findUserByName("Alice");
        double initial = u.getOutstandingFine();
        
        userService.addFine(u, 10.0);
        assertEquals(initial + 10.0, u.getOutstandingFine());
        
        userService.addFine(u, -5.0); // Should be ignored (for coverage)
        assertEquals(initial + 10.0, u.getOutstandingFine());
    }
    
    @Test
    @DisplayName("Save Users Exception Handling")
    void testSaveException() {
        UserService badPathService = new UserService("Z:/invalid/path/users.txt");
        badPathService.getAllUsers().add(new User("Test", "pass", "User"));
        
        assertDoesNotThrow(() -> badPathService.saveUsersToFile());
        assertTrue(outContent.toString().contains("Error saving users file"));
    }
}