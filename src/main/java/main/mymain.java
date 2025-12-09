package main;

import service.*;
import model.*;

import java.util.Scanner;

public class mymain {

    static Scanner scanner = new Scanner(System.in);
    
    // 🛑 1. إضافة متغير للتحكم في استمرار عمل التطبيق (لحل مشكلة Blocker)
    private static boolean isRunning = true; 
    
    private static final String INVALID_INPUT_MSG = "❌ Invalid input or choice."; 
    private static final String ADMIN_LOGIN_REQUIRED_MSG = "⚠ Please log in as Admin first!";

    // ===== Shared Services =====
    static AdminService adminService = new AdminService();
    static UserService userService = new UserService();
    static BookService bookService = new BookService(adminService, userService);

    public static void main(String[] args) {

        while (isRunning) { // 🛑 التعديل هنا: while (isRunning)
            User loggedInUser = handleLoginProcess(); // تم استخراج عملية الدخول

            if (loggedInUser != null) {
                String role = loggedInUser.getRole();
                switch (role.toLowerCase()) {
                    case "admin":
                        adminService.loginAdmin(loggedInUser);
                        adminMenu();
                        break;
                    case "user":
                        userMenu(loggedInUser);
                        break;
                    case "librarian":
                        librarianMenu(loggedInUser);
                        break;
                    default:
                        System.out.println("❌ Unknown role! Returning to login screen.");
                }
            }
            // طباعة رسالة العودة طالما التطبيق مستمر
            if (isRunning) {
                System.out.println("\n🔄 Returning to Login screen...\n");
            }
        }
        System.out.println("👋 Thank you for using the Library System. Goodbye!");
    }
    
    // ===============================================================
    // دوال مساعدة رئيسية (لحل مشكلة التعقيد)
    // ===============================================================

    private static User handleLoginProcess() {
        while (true) {
            System.out.println("\n=== LIBRARY SYSTEM LOGIN ===");
            //System.out.println("0. Exit System"); // 🛑 إضافة خيار الخروج للقائمة
            System.out.print("Enter Username: ");
            String input = scanner.nextLine();
            
            if ("0".equals(input.trim())) {
                isRunning = false; // 🛑 تغيير حالة التطبيق للخروج
                return null;
            }

            String username = input;
            System.out.print("Enter Password: ");
            String password = scanner.nextLine();

            User loggedInUser = userService.login(username, password, bookService);

            if (loggedInUser != null) {
                return loggedInUser;
            }
        }
    }
    
    // دالة مساعدة لقراءة خيار القائمة والتعامل مع أخطاء الإدخال
    private static int readMenuChoice() {
        int choice = -1;
        try {
            choice = scanner.nextInt();
            scanner.nextLine();
            return choice;
        } catch (Exception e) {
            scanner.nextLine();
            System.out.println(INVALID_INPUT_MSG);
            return -1; 
        }
    }
    
    // =================== ADMIN MENU ===================
    public static void adminMenu() {
        while (true) {
            displayAdminMenuOptions();
            int choice = readMenuChoice();

            if (choice == -1) continue;

            switch (choice) {
                case 1: handleAddBook(); break;
                case 2: handleAddCD(); break;
                case 3: handleSearchMedia(); break;
                case 4: handleSendReminders(); break;
                case 5: handleUnregisterUser(); break;
                case 6: handleViewAllMedia(); break;
                case 7: 
                    String logoutResult = adminService.logout(); 
                    System.out.println(logoutResult);
                    return; // الخروج من adminMenu والعودة لـ main
                default: System.out.println(INVALID_INPUT_MSG);
            }
        }
    }

    // =================== دوال معالجة خيارات الإداري المستخرجة ===================

    private static void displayAdminMenuOptions() {
        System.out.println("\n===== ADMIN MENU =====");
        System.out.println("1. Add Book");
        System.out.println("2. Add CD");
        System.out.println("3. Search Media");
        System.out.println("4. Send Reminder Emails");
        System.out.println("5. Unregister User");
        System.out.println("6. View All Media (Books & CDs)");
        System.out.println("7. Logout");
        System.out.println("======================");
        System.out.print("Enter your choice: ");
    }

    private static void handleAddBook() {
        if (!adminService.isLoggedIn()) {
            System.out.println(ADMIN_LOGIN_REQUIRED_MSG);
            return;
        }
        System.out.print("Enter Book Title: ");
        String title = scanner.nextLine();
        System.out.print("Enter Author: ");
        String author = scanner.nextLine();
        System.out.print("Enter ISBN: ");
        String isbn = scanner.nextLine();
        
        String result = bookService.addBook(title, author, isbn);
        System.out.println(result); 
    }

    private static void handleAddCD() {
        if (!adminService.isLoggedIn()) {
            System.out.println(ADMIN_LOGIN_REQUIRED_MSG);
            return;
        }
        System.out.print("Enter CD Title: ");
        String cdTitle = scanner.nextLine();
        System.out.print("Enter Artist: ");
        String artist = scanner.nextLine();
        System.out.print("Enter Barcode: ");
        String barcode = scanner.nextLine();
        
        String result = bookService.addCD(cdTitle, artist, barcode);
        System.out.println(result);
    }

    private static void handleSearchMedia() {
        System.out.print("Enter search keyword: ");
        String keyword = scanner.nextLine();
        bookService.searchBook(keyword);
    }

    private static void handleSendReminders() {
        String result = adminService.sendOverdueReminders(userService, bookService);
        System.out.println(result);
    }

    private static void handleUnregisterUser() {
        System.out.println("\n=== Unregister User ===");
        System.out.print("Enter username to delete: ");
        String userToDelete = scanner.nextLine();
        
        String result = adminService.unregisterUser(userToDelete, userService, bookService);
        System.out.println(result);
    }

    private static void handleViewAllMedia() {
        System.out.println("📚 All Media Status:");
        boolean hasItems = false;
        
        for (media m : bookService.getAllBooks()) { 
            hasItems = true;
            String status;
            if (m.isBorrowed()) {
                status = "🔴 Borrowed by " + (m.getBorrowedBy() != null ? m.getBorrowedBy().getName() : "Unknown") +
                         " | Due: " + m.getDueDate();
                if (m.isOverdue()) status += " ⚠ OVERDUE";
                if (m.isFineIssued()) status += " ($ Fine Calc)";
            } else {
                status = "🟢 Available";
            }
            System.out.println(m.toString() + " | " + status);
        }
        if (!hasItems) System.out.println("No items in library.");
    }


 // =================== USER MENU ===================
    public static void userMenu(User user) {
        while (true) {
            System.out.println("\n===== USER MENU (" + user.getName() + ") =====");
            System.out.println("1. Search Media");
            System.out.println("2. Borrow Item (Book/CD)");
            System.out.println("3. Return Item");
            System.out.println("4. Pay Fine");
            System.out.println("5. Logout");
            System.out.println("======================");
            System.out.print("Enter choice: ");

            int choice = readMenuChoice();

            switch (choice) {
                case 1:
                    System.out.print("Enter search keyword: ");
                    String keyword = scanner.nextLine();
                    bookService.searchBook(keyword);
                    break;

                case 2:
                    if (user.getOutstandingFine() > 0) {
                        System.out.println("❌ BLOCKED: You cannot borrow items.");
                        System.out.println("💰 You have unpaid fines: $" + user.getOutstandingFine());
                        System.out.println("👉 Please go to Option 4 to pay first.");
                        break;
                    }
                    System.out.print("Enter ISBN (Book) or Barcode (CD) to borrow: ");
                    String id = scanner.nextLine();
                    String borrowResult = bookService.borrowBook(user, id); 
                    System.out.println(borrowResult); // طباعة النتيجة
                    break;

                case 3:
                    if (user.getOutstandingFine() > 0) {
                        System.out.println("❌ ACTION DENIED: You cannot return items while you have unpaid fines.");
                        System.out.println("💰 Your Outstanding Fine: $" + user.getOutstandingFine());
                        System.out.println("👉 Please go to Option 4 (Pay Fine) and clear your balance first.");
                        break; 
                    }

                    System.out.print("Enter ISBN or Barcode to return: ");
                    String returnId = scanner.nextLine();
                    String returnResult = bookService.returnBook(returnId, user);
                    System.out.println(returnResult); // طباعة النتيجة
                    break;

                case 4:
                    double fine = user.getOutstandingFine();
                    if (fine <= 0) {
                        System.out.println("✅ You have no fines to pay.");
                    } else {
                        System.out.println("💰 Your outstanding fine: " + fine);
                        System.out.print("Enter amount to pay: ");
                        try {
                            double amount = scanner.nextDouble();
                            scanner.nextLine();
                            String fineResult = userService.payFine(user, amount, bookService);
                            System.out.println(fineResult); // طباعة النتيجة
                        } catch (Exception e) {
                            scanner.nextLine();
                            System.out.println(INVALID_INPUT_MSG);
                        }
                    }
                    break;

                case 5:
                    String logoutResult = userService.logout();
                    System.out.println(logoutResult);
                    return; // الخروج من userMenu والعودة لـ main

                default:
                    System.out.println(INVALID_INPUT_MSG);
            }
        }
    }
 
    // =================== LIBRARIAN MENU ===================
    public static void librarianMenu(User librarian) {
        LibrarianService librarianService = new LibrarianService();
        librarianService.loginLibrarian(librarian);

        while (true) {
            System.out.println("\n===== LIBRARIAN MENU =====");
            System.out.println("1. Show Overdue Items");
            System.out.println("2. Issue Fines (Calculate & Show)");
            System.out.println("3. Logout");
            System.out.println("===========================");
            System.out.print("Enter choice: ");

            int choice = readMenuChoice();

            switch (choice) {
                case 1:
                    librarianService.showOverdueBooks(bookService.getAllBooks());
                    break;

                case 2:
                    System.out.println("🔄 System is calculating fines for all users...");
                    userService.checkAndApplyFinesForAllUsers(bookService);
                    
                    librarianService.issueFines(bookService.getAllBooks(), userService);
                    break;

                case 3:
                    librarianService.logout();
                    return;

                default:
                    System.out.println("❌ Invalid choice.");
            }
        }
    }
}