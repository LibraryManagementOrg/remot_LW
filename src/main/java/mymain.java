import service.*;
import model.*;

import java.util.Scanner;

public class mymain {

    static Scanner scanner = new Scanner(System.in);

    // ===== Shared Services =====
    static AdminService adminService = new AdminService();
    static UserService userService = new UserService();
    static BookService bookService = new BookService(adminService, userService);
    static LoginService loginService = new LoginService();

    // ✅ جديد: تعريف خدمات التنبيه والإيميل
    static NotificationObserver emailService = new RealEmailService(); // أو RealEmailService لو استخدمتي الحقيقي
    static ReminderService reminderService =  new ReminderService(emailService, userService);

    public static void main(String[] args) {

        while (true) {
            User loggedInUser = null;

            // ===== START SCREEN (Login or Register) =====
            System.out.println("\n==================================");
            System.out.println("    LIBRARY MANAGEMENT SYSTEM     ");
            System.out.println("==================================");
            System.out.println("1. Login");
            System.out.println("2. Create New Account (Register)");
            System.out.println("3. Exit");
            System.out.print("Choose an option: ");

            int startChoice;
            try {
                startChoice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                continue;
            }

            // --- خيار إنشاء حساب جديد ---
            if (startChoice == 2) {
                System.out.println("\n=== Create New Account ===");
                System.out.print("Enter Name: ");
                String name = scanner.nextLine();

                // ✅ جديد: طلب الإيميل
                System.out.print("Enter Email: ");
                String email = scanner.nextLine();

                System.out.print("Enter Password: ");
                String password = scanner.nextLine();

                // إنشاء وحفظ المستخدم
                User newUser = new User(name, email, password, "User");
                userService.addUser(newUser);
                System.out.println("✅ Account created successfully! Please login.");
                continue; // العودة للقائمة الرئيسية لتسجيل الدخول
            } 
            else if (startChoice == 3) {
                System.out.println("Goodbye!");
                System.exit(0);
            }
            else if (startChoice != 1) {
                System.out.println("❌ Invalid option.");
                continue;
            }

            // ===== LOGIN LOOP =====
            while (true) {
                System.out.println("\n--- LOGIN ---");
                System.out.print("Enter Username: ");
                String username = scanner.nextLine();

                System.out.print("Enter Password: ");
                String password = scanner.nextLine();

                loggedInUser = loginService.login(username, password);
                if (loggedInUser != null) {

                    switch (loggedInUser.getRole().toLowerCase()) {
                        case "user":
                            // تحديث بيانات اليوزر من القائمة للتأكد من وجود الإيميل والكتب
                            loggedInUser = userService.findUserByName(loggedInUser.getName());
                            userService.login(loggedInUser.getName(), loggedInUser.getPassword());
                            break;

                        case "admin":
                            adminService.loginAdmin(loggedInUser);
                            break;

                        case "librarian":
                            // Librarian session handled later
                            break;

                        default:
                            System.out.println("❌ Unknown role! Logging out.");
                            loggedInUser = null;
                            continue;
                    }
                    break; // كسر حلقة اللوجين والدخول للقوائم
                }
                System.out.println("❌ Wrong username or password! Try again.");
            }

            // تحديد القائمة حسب الدور
            String role = loggedInUser.getRole();

            switch (role.toLowerCase()) {
                case "admin":
                    adminMenu();
                    break;

                case "user":
                    userMenu(loggedInUser);
                    break;

                case "librarian":
                    librarianMenu(loggedInUser);
                    break;
            }

            System.out.println("\n🔄 Returning to Main Screen...\n");
        }
    }

    // =================== ADMIN MENU ===================
    public static void adminMenu() {
        while (true) {
            System.out.println("\n===== ADMIN MENU =====");
            System.out.println("1. Add Book");
            System.out.println("2. Add CD");
            System.out.println("3. Search Media (Books + CDs)");
            System.out.println("4. Send Reminder Emails (Observer Pattern)"); // ✅
            System.out.println("5. Unregister User");
            System.out.println("6. View All Books & Overdue");
            System.out.println("7. Logout");
            System.out.println("======================");
            System.out.print("Enter your choice: ");

            int choice;
            try {
                choice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input.");
                continue;
            }

            switch (choice) {
                case 1:
                    if (!adminService.isLoggedIn()) {
                        System.out.println("⚠ Please log in as Admin first!");
                        break;
                    }
                    System.out.print("Enter Book Title: ");
                    String title = scanner.nextLine();
                    System.out.print("Enter Author: ");
                    String author = scanner.nextLine();
                    System.out.print("Enter ISBN: ");
                    String isbn = scanner.nextLine();
                    bookService.addBook(title, author, isbn);
                    break;

                case 2:
                    System.out.println("🎵 Add CD feature coming soon...");
                    break;

                case 3:
                    System.out.print("Enter search keyword: ");
                    String keyword = scanner.nextLine();
                    bookService.searchBook(keyword);
                    break;

                case 4:
                    // ✅ تفعيل الكود الخاص بالسبرنت 3
                    System.out.println("📩 Sending overdue reminders...");
                    // نمرر قائمة الكتب كلها للخدمة وهي تفحص المتأخر وترسل إيميلات
                    reminderService.sendOverdueReminders(bookService.getAllBooks());
                    break;

                case 5:
                    System.out.println("❗ Unregister User feature coming soon...");
                    break;

                case 6:
                    System.out.println("📚 All Books:");
                    for (Book b : bookService.getAllBooks()) {
                        String status = b.isBorrowed() ?
                                "Borrowed | Due: " + b.getDueDate() + (b.isOverdue() ? " ⚠ Overdue!" : "")
                                : "Available";
                        System.out.println(b.getTitle() + " by " + b.getAuthor() + " | ISBN: " + b.getIsbn() + " | " + status);
                    }
                    break;

                case 7:
                    adminService.logout();
                    return;

                default:
                    System.out.println("❌ Invalid option, try again.");
            }
        }
    }

    // =================== USER MENU ===================
    public static void userMenu(User user) {
        while (true) {
            System.out.println("\n===== USER MENU (" + user.getName() + ") =====");
            System.out.println("1. Search Book");
            System.out.println("2. Borrow Book");
            System.out.println("3. Return Book");
            System.out.println("4. Pay Fine");
            System.out.println("5. Logout");
            System.out.println("======================");
            System.out.print("Enter choice: ");

            int choice;
            try {
                choice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input.");
                continue;
            }

            switch (choice) {
                case 1:
                    System.out.print("Enter search keyword: ");
                    String keyword = scanner.nextLine();
                    bookService.searchBook(keyword);
                    break;

                case 2:
                    if (!user.canBorrow()) {
                        System.out.println("❌ You cannot borrow books until you pay your fines. Outstanding fine: " + user.getOutstandingFine());
                        break;
                    }
                    System.out.print("Enter ISBN of the book to borrow: ");
                    String isbn = scanner.nextLine();
                    bookService.borrowBook(user, isbn);
                    break;

                case 3:
                    System.out.print("Enter ISBN of the book to return: ");
                    String returnIsbn = scanner.nextLine();
                    bookService.returnBook(returnIsbn, user);
                    break;

                case 4:
                    double fine = user.getOutstandingFine();
                    if (fine == 0) {
                        System.out.println("✅ You have no fines.");
                    } else {
                        System.out.println("Your outstanding fine: " + fine);
                        System.out.print("Enter amount to pay: ");
                        double amount;
                        try {
                            amount = Double.parseDouble(scanner.nextLine());
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid amount.");
                            break;
                        }
                        userService.payFine(user, amount);
                    }
                    break;

                case 5:
                    userService.logout();
                    return;

                default:
                    System.out.println("❌ Invalid choice.");
            }
        }
    }

    // =================== LIBRARIAN MENU ===================
    public static void librarianMenu(User librarian) {
        LibrarianService librarianService = new LibrarianService();
        librarianService.loginLibrarian(librarian);

        while (true) {
            System.out.println("\n===== LIBRARIAN MENU =====");
            System.out.println("1. Show Overdue Books");
            System.out.println("2. Issue Fines");
            System.out.println("3. Logout");
            System.out.println("===========================");
            System.out.print("Enter choice: ");

            int choice;
            try {
                choice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input.");
                continue;
            }

            switch (choice) {
                case 1:
                    librarianService.showOverdueBooks(bookService.getAllBooks());
                    break;

                case 2:
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