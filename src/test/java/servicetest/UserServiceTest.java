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
    
    // لالتقاط المخرجات (System.out)
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    @BeforeEach
    void setUp() throws IOException {
        // 1. إنشاء ملف وهمي ببيانات أولية قبل كل اختبار
        createDummyFile();
        
        // 2. توجيه الطباعة لالتقاط الرسائل
        System.setOut(new PrintStream(outContent));

        // 3. تهيئة الخدمة مع الملف الوهمي
        userService = new UserService(TEST_FILE);
    }

    @AfterEach
    void tearDown() {
        // استعادة الطباعة
        System.setOut(originalOut);
        
        // حذف الملف الوهمي
        File f = new File(TEST_FILE);
        if (f.exists()) f.delete();
    }

    private void createDummyFile() throws IOException {
        try (PrintWriter pw = new PrintWriter(new FileWriter(TEST_FILE))) {
            pw.println("Alice,123,User,0.0,alice@test.com");
            pw.println("Bob,pass,User,50.0,bob@test.com");
            pw.println("Admin,admin,Admin,0.0,admin@test.com");
            // سطر ناقص (Invalid Data) لاختبار المناعة ضد الأخطاء
            pw.println("ShortLine,Data"); 
            // سطر بغرامة خاطئة (Invalid Number)
            pw.println("BadFine,pass,User,NotANumber,email");
        }
    }

    // =========================================================
    // 1. اختبار تحميل البيانات (Loading & Parsing)
    // =========================================================
    @Test
    @DisplayName("Load Users: Valid, Invalid Lines, and Bad Number Format")
    void testLoadUsersRobustness() {
        // المستخدمين الصحيحين فقط يجب أن يتم تحميلهم
        assertNotNull(userService.findUserByName("Alice"));
        assertNotNull(userService.findUserByName("Bob"));
        
        // المستخدم صاحب السطر الناقص يجب ألا يكون موجوداً
        assertNull(userService.findUserByName("ShortLine"));

        // المستخدم صاحب الغرامة الخاطئة يجب أن يتم تحميله بغرامة 0.0
        User badFineUser = userService.findUserByName("BadFine");
        assertNotNull(badFineUser);
        assertEquals(0.0, badFineUser.getOutstandingFine());
    }

    // =========================================================
    // 2. اختبار تسجيل الدخول والخروج
    // =========================================================
    @Test
    @DisplayName("Login: Success and Fail Scenarios")
    void testLogin() {
        // نجاح
        User u = userService.login("Alice", "123", null);
        assertNotNull(u);
        assertTrue(userService.isLoggedIn());
        assertEquals("Alice", userService.getLoggedInUser().getName());

        // فشل (كلمة سر خطأ)
        assertNull(userService.login("Alice", "wrong", null));

        // فشل (مستخدم غير موجود)
        assertNull(userService.login("Ghost", "123", null));
    }

    @Test
    @DisplayName("Logout Logic")
    void testLogout() {
        userService.login("Alice", "123", null);
        userService.logout();
        assertFalse(userService.isLoggedIn());
        assertNull(userService.getLoggedInUser());
    }
    
    @Test
    @DisplayName("Login triggers fine check if BookService provided")
    void testLoginTriggersFineCheck() {
        // BookService وهمي للتأكد من استدعاء دالة فحص الغرامات
        BookService stubBookService = new BookService(null, null, "dummy.txt") {
            @Override
            public void saveBooksToFile() { /* Do nothing */ }
            @Override
            public List<media> getAllBooks() { return new ArrayList<>(); }
        };

        userService.login("Alice", "123", stubBookService);
        // لا يمكننا التحقق من الاستدعاء مباشرة إلا إذا استخدمنا Mockito،
        // لكننا نضمن أن الكود يمر عبر فرع if (bookService != null) دون أن ينهار.
        assertTrue(userService.isLoggedIn());
    }

    // =========================================================
    // 3. اختبار دفع الغرامات (Pay Fine) - المنطق المعقد
    // =========================================================
    @Test
    @DisplayName("Pay Fine: Validation Checks")
    void testPayFineValidation() {
        User bob = userService.findUserByName("Bob"); // عليه 50.0

        // 1. محاولة الدفع بدون تسجيل دخول
        userService.logout();
        userService.payFine(bob, 10, null);
        assertTrue(outContent.toString().contains("Access denied"));

        // تسجيل دخول بوب
        userService.login("Bob", "pass", null);
        
        // 2. مبلغ سالب
        userService.payFine(bob, -5, null);
        assertTrue(outContent.toString().contains("Invalid amount"));

        // 3. مبلغ أكبر من الغرامة
        userService.payFine(bob, 100, null);
        assertTrue(outContent.toString().contains("Error: You entered"));
    }

    @Test
    @DisplayName("Pay Fine: Full Payment & Auto-Return Books")
    void testPayFineSuccessWithAutoReturn() {
        User bob = userService.findUserByName("Bob"); // عليه 50.0
        userService.login("Bob", "pass", null);

        // إعداد كتاب متأخر ومستعار بواسطة بوب
        Book overdueBook = new Book("Java", "Auth", "111");
        overdueBook.setBorrowed(true);
        overdueBook.setBorrowedBy(bob);
        overdueBook.setDueDate(LocalDate.now().minusDays(5)); // متأخر
        overdueBook.setFineIssued(true);

        // Stubbing BookService
        BookService stubBookService = new BookService(null, null, "dummy.txt") {
            @Override
            public List<media> getAllBooks() {
                List<media> list = new ArrayList<>();
                list.add(overdueBook);
                return list;
            }
            @Override
            public void saveBooksToFile() { /* Mock save */ }
        };

        // دفع كامل المبلغ (50.0)
        userService.payFine(bob, 50.0, stubBookService);

        // التحقق:
        // 1. الغرامة أصبحت 0
        assertEquals(0.0, bob.getOutstandingFine());
        // 2. الكتاب تم إرجاعه تلقائياً (Auto-returned)
        assertFalse(overdueBook.isBorrowed());
        assertNull(overdueBook.getBorrowedBy());
        assertTrue(outContent.toString().contains("Payment successful"));
        assertTrue(outContent.toString().contains("Automatically returned"));
    }

    // =========================================================
    // 4. اختبار حساب الغرامات (Check and Apply Fines)
    // =========================================================
    @Test
    @DisplayName("Apply Fines for Overdue Books")
    void testCheckAndApplyFines() {
        User alice = userService.findUserByName("Alice"); // غرامتها 0.0

        // إعداد كتاب متأخر لأليس، ولم تُحسب غرامته بعد (!isFineIssued)
        Book lateBook = new Book("Late Book", "Auth", "999");
        lateBook.borrow(alice);
        lateBook.setDueDate(LocalDate.now().minusDays(2)); // متأخر يومين
        // غرامة اليوم = 10، يومين = 20

        BookService stubBookService = new BookService(null, null, "dummy.txt") {
            @Override
            public List<media> getAllBooks() {
                return Collections.singletonList(lateBook);
            }
            @Override
            public void saveBooksToFile() { /* Mock save */ }
        };

        // تشغيل الدالة
        userService.checkAndApplyFinesForAllUsers(stubBookService);

        // التحقق
        // 1. أليس يجب أن تزيد غرامتها بمقدار 20
        assertEquals(20.0, alice.getOutstandingFine());
        // 2. الكتاب يجب أن يُعلم بأنه تم إصدار غرامة له
        assertTrue(lateBook.isFineIssued());
    }

    // =========================================================
    // 5. اختبار الحذف والإضافة (Crud Operations)
    // =========================================================
    @Test
    @DisplayName("Delete User: Success and Fail")
    void testDeleteUser() {
        // نجاح
        assertTrue(userService.deleteUser("Alice"));
        assertNull(userService.findUserByName("Alice"));

        // فشل (غير موجود)
        assertFalse(userService.deleteUser("Ghost"));
    }

    @Test
    @DisplayName("Add Fine Helper")
    void testAddFine() {
        User u = userService.findUserByName("Alice");
        double initial = u.getOutstandingFine();
        
        userService.addFine(u, 10.0);
        assertEquals(initial + 10.0, u.getOutstandingFine());
        
        userService.addFine(u, -5.0); // Should be ignored
        assertEquals(initial + 10.0, u.getOutstandingFine());
    }

    @Test
    @DisplayName("Save Users Exception Handling")
    void testSaveException() {
        // لاختبار الـ IOException، يمكننا استخدام مسار غير صالح
        UserService badPathService = new UserService("Z:/invalid/path/users.txt");
        badPathService.getAllUsers().add(new User("Test", "pass", "User"));
        
        // يجب أن يطبع الخطأ ولا ينهار البرنامج
        assertDoesNotThrow(() -> badPathService.saveUsersToFile());
        assertTrue(outContent.toString().contains("Error saving users file"));
    }
}