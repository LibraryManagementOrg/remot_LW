//package servicetest;
/*
import model.Book;
import model.User;
import service.UserService;

import org.junit.jupiter.api.*;

import java.io.*;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    private UserService userService;
    private final String TEST_FILE_PATH = "src/main/resources/users_test.txt";

    @BeforeEach
    void setUp() {
        userService = new UserService(TEST_FILE_PATH);
        userService.getAllUsers().clear();
    }

    @AfterEach
    void tearDown() {
        File f = new File(TEST_FILE_PATH);
        if (f.exists()) f.delete();
    }

    // ---------------------------------------------------------
    // 1. LOGIN — Successful
    // ---------------------------------------------------------
    @Test
    void testLoginSuccess() {
        userService.getAllUsers().add(new User("Layal", "123", "User"));

        boolean result = userService.login("Layal", "123");

        assertTrue(result);
        assertNotNull(userService.getLoggedInUser());
    }

    // ---------------------------------------------------------
    // 2. LOGIN — Wrong password
    // ---------------------------------------------------------
    @Test
    void testLoginWrongPassword() {
        userService.getAllUsers().add(new User("Ali", "999", "User"));

        boolean result = userService.login("Ali", "123");

        assertFalse(result);
        assertNull(userService.getLoggedInUser());
    }

    // ---------------------------------------------------------
    // 3. LOGIN — User not found
    // ---------------------------------------------------------
    @Test
    void testLoginUserNotFound() {
        boolean result = userService.login("X", "123");
        assertFalse(result);
    }

    // ---------------------------------------------------------
    // 4. LOGIN — Wrong role type
    // ---------------------------------------------------------
    @Test
    void testLoginWrongRole() {
        userService.getAllUsers().add(new User("Admin", "555", "Admin"));

        boolean result = userService.login("Admin", "555");

        assertFalse(result); // userService يقبل فقط User role
    }

    // ---------------------------------------------------------
    // 5. LOGOUT
    // ---------------------------------------------------------
    @Test
    void testLogout() {
        userService.getAllUsers().add(new User("Sara", "111", "User"));
        userService.login("Sara", "111");
        userService.logout();

        assertNull(userService.getLoggedInUser());
    }

    // ---------------------------------------------------------
    // 6. FIND USER — Exists
    // ---------------------------------------------------------
    @Test
    void testFindUserExists() {
        User u = new User("Omar", "555", "User");
        userService.getAllUsers().add(u);

        assertEquals(u, userService.findUserByName("Omar"));
    }

    // ---------------------------------------------------------
    // 7. FIND USER — Not found
    // ---------------------------------------------------------
    @Test
    void testFindUserNotFound() {
        assertNull(userService.findUserByName("Nope"));
    }

    // ---------------------------------------------------------
    // 8. PAY FINE — Enough balance
    // ---------------------------------------------------------
    @Test
    void testPayFineEnoughBalance() {
        User u = new User("Maya", "444", "User");
        u.setOutstandingFine(10);
        u.setWalletBalance(50);

        boolean ok = userService.payFine(u);

        assertTrue(ok);
        assertEquals(40, u.getWalletBalance());
        assertEquals(0, u.getOutstandingFine());
    }

    // ---------------------------------------------------------
    // 9. PAY FINE — Not enough balance
    // ---------------------------------------------------------
    @Test
    void testPayFineNotEnoughBalance() {
        User u = new User("Lina", "333", "User");
        u.setOutstandingFine(30);
        u.setWalletBalance(10);

        boolean ok = userService.payFine(u);

        assertFalse(ok);
        assertEquals(10, u.getWalletBalance());
        assertEquals(30, u.getOutstandingFine());
    }

    // ---------------------------------------------------------
    // 10. PAY FINE — No fines
    // ---------------------------------------------------------
    @Test
    void testPayFineNoFine() {
        User u = new User("Nada", "222", "User");
        u.setOutstandingFine(0);
        u.setWalletBalance(20);

        boolean ok = userService.payFine(u);

        assertFalse(ok);
    }

    // ---------------------------------------------------------
    // 11. CHECK & APPLY FINES — Overdue exists
    // ---------------------------------------------------------
    @Test
    void testCheckAndApplyFines_WithOverdue() {
        User u = new User("Khaled", "111", "User");

        Book b = new Book("Java", "A", "B1");
        b.setBorrowed(true);
        b.setBorrowedBy("Khaled");
        b.setDueDate(LocalDate.now().minusDays(2)); // يومين تأخير

        u.getBorrowedItems().add(b);
        userService.getAllUsers().add(u);

        userService.checkAndApplyFines(u);

        assertEquals(4.0, u.getOutstandingFine()); // 2 days * 2$
    }

    // ---------------------------------------------------------
    // 12. CHECK & APPLY FINES — No overdue
    // ---------------------------------------------------------
    @Test
    void testCheckAndApplyFines_NoOverdue() {
        User u = new User("Mona", "888", "User");

        Book b = new Book("Network", "A", "B2");
        b.setBorrowed(true);
        b.setBorrowedBy("Mona");
        b.setDueDate(LocalDate.now().plusDays(3));

        u.getBorrowedItems().add(b);
        userService.getAllUsers().add(u);

        userService.checkAndApplyFines(u);

        assertEquals(0, u.getOutstandingFine());
    }

    // ---------------------------------------------------------
    // 13. LOAD USERS FROM FILE
    // ---------------------------------------------------------
    @Test
    void testLoadUsers() throws Exception {
        try (FileWriter fw = new FileWriter(TEST_FILE_PATH)) {
            fw.write("Sam,123,User,20,10\n");
            fw.write("Lama,999,User,0,5\n");
        }

        UserService s2 = new UserService(TEST_FILE_PATH);

        assertEquals(2, s2.getAllUsers().size());
        assertEquals("Sam", s2.getAllUsers().get(0).getName());
    }

    // ---------------------------------------------------------
    // 14. SAVE USERS TO FILE
    // ---------------------------------------------------------
    @Test
    void testSaveUsers() throws Exception {
        userService.getAllUsers().add(new User("Rami", "222", "User"));
        userService.saveUsersToFile();

        File f = new File(TEST_FILE_PATH);
        assertTrue(f.exists());
        assertTrue(f.length() > 0);
    }

    // ---------------------------------------------------------
    // 15. GET ALL USERS
    // ---------------------------------------------------------
    @Test
    void testGetAllUsers() {
        userService.getAllUsers().add(new User("X", "1", "User"));
        List<User> list = userService.getAllUsers();

        assertEquals(1, list.size());
    }
}*/
