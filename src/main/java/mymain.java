import service.*;
import model.*;

import java.util.Scanner;

public class mymain {

    static Scanner scanner = new Scanner(System.in);

    // ===== Shared Services =====
    static AdminService adminService = new AdminService();
    static UserService userService = new UserService();
    
    // نمرر الخدمات لبعضها البعض حسب الحاجة
    // تأكد أن BookService لديك يحتوي على هذا الكونستركتور، أو عدله حسب الموجود لديك
    static BookService bookService = new BookService(adminService, userService);

    public static void main(String[] args) {

        while (true) {
            User loggedInUser = null;

            // ===== LOGIN LOOP =====
            while (true) {
                System.out.println("\n=== LIBRARY SYSTEM LOGIN ===");
                System.out.println("Enter Username:");
                String username = scanner.nextLine();

                System.out.println("Enter Password:");
                String password = scanner.nextLine();

                // تمرير bookService يسمح بتحديث الغرامات بصمت عند دخول المستخدم
                loggedInUser = userService.login(username, password, bookService);

                if (loggedInUser != null) break;
            }

            // تحديد الدور وتوجيه المستخدم للقائمة المناسبة
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

            System.out.println("\n🔄 Returning to Login screen...\n");
        }
    }

    // =================== ADMIN MENU ===================
    public static void adminMenu() {
        while (true) {
            System.out.println("\n===== ADMIN MENU =====");
            System.out.println("1. Add Book");
            System.out.println("2. Add CD");
            System.out.println("3. Search Media");
            System.out.println("4. Send Reminder Emails");
            System.out.println("5. Unregister User");
            System.out.println("6. View All Books & Overdue");
            System.out.println("7. Logout");
            System.out.println("======================");
            System.out.print("Enter your choice: ");

            int choice = -1;
            try {
                choice = scanner.nextInt();
                scanner.nextLine(); // consume newline
            } catch (Exception e) {
                scanner.nextLine();
                System.out.println("❌ Invalid input.");
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
                    System.out.println("📩 Reminder sending (mock) not implemented yet.");
                    break;

                case 5:
                    System.out.println("❗ Unregister User feature coming soon...");
                    break;

                case 6:
                    System.out.println("📚 All Books Status:");
                    boolean hasBooks = false;
                    for (Book b : bookService.getAllBooks()) {
                        hasBooks = true;
                        String status;
                        if (b.isBorrowed()) {
                            status = "🔴 Borrowed by " + (b.getBorrowedBy() != null ? b.getBorrowedBy().getName() : "Unknown") +
                                     " | Due: " + b.getDueDate();
                            if (b.isOverdue()) status += " ⚠ OVERDUE";
                            if (b.isFineIssued()) status += " ($ Fine Calc)";
                        } else {
                            status = "🟢 Available";
                        }
                        System.out.println("- " + b.getTitle() + " | " + status);
                    }
                    if (!hasBooks) System.out.println("No books in library.");
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
            System.out.println("\n===== USER MENU =====");
            System.out.println("1. Search Book");
            System.out.println("2. Borrow Book");
            System.out.println("3. Return Book");
            System.out.println("4. Pay Fine");
            System.out.println("5. Logout");
            System.out.println("======================");
            System.out.print("Enter choice: ");

            int choice = -1;
            try {
                choice = scanner.nextInt();
                scanner.nextLine();
            } catch (Exception e) {
                scanner.nextLine();
                System.out.println("❌ Invalid input.");
                continue;
            }

            switch (choice) {
                case 1:
                    System.out.print("Enter search keyword: ");
                    String keyword = scanner.nextLine();
                    bookService.searchBook(keyword);
                    break;

                case 2:
                    if (user.getOutstandingFine() > 0) {
                        System.out.println("❌ You cannot borrow books until you pay your fines. Outstanding fine: " + user.getOutstandingFine());
                        break;
                    }
                    System.out.print("Enter ISBN of the book to borrow: ");
                    String isbn = scanner.nextLine();
                    bookService.borrowBook(user, isbn);
                    break;

                case 3:
                    if (user.getOutstandingFine() > 0) {
                        System.out.println("❌ You cannot return books until you pay your fines. Outstanding fine: " + user.getOutstandingFine());
                        break;
                    }
                    System.out.print("Enter ISBN of the book to return: ");
                    String returnIsbn = scanner.nextLine();
                    bookService.returnBook(returnIsbn, user);
                    break;

                case 4:
                    double fine = user.getOutstandingFine();
                    if (fine <= 0) {
                        System.out.println("✅ You have no fines to pay.");
                    } else {
                        System.out.println("💰 Your outstanding fine: " + fine);
                        System.out.print("Enter amount to pay: ");
                        double amount = -1;
                        try {
                            amount = scanner.nextDouble();
                            scanner.nextLine();
                        } catch (Exception e) {
                            scanner.nextLine();
                            System.out.println("❌ Invalid number.");
                            break;
                        }
                        userService.payFine(user, amount, bookService);
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
            System.out.println("2. Issue Fines (Calculate & Show)");
            System.out.println("3. Logout");
            System.out.println("===========================");
            System.out.print("Enter choice: ");

            int choice = -1;
            try {
                choice = scanner.nextInt();
                scanner.nextLine();
            } catch (Exception e) {
                scanner.nextLine();
                System.out.println("❌ Invalid input.");
                continue;
            }

            switch (choice) {
                case 1:
                    librarianService.showOverdueBooks(bookService.getAllBooks());
                    break;

                case 2:
                    // 🌟🌟 التعديل الحاسم هنا 🌟🌟
                    // قبل عرض الغرامات، نقوم بحسابها وتحديث الملفات لجميع المستخدمين والكتب
                    System.out.println("🔄 System is calculating fines for all users...");
                    userService.checkAndApplyFinesForAllUsers(bookService);
                    
                    // الآن نعرض الغرامات (ستكون محدثة حتى لو المستخدم لم يدخل بعد)
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