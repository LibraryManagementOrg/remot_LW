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
    static LibrarianService librarianService = new LibrarianService(userService);

    public static void main(String[] args) {

        while (true) {
            User loggedInUser = null;

            // ===== LOGIN LOOP =====
            while (true) {
                System.out.println("Enter Username:");
                String username = scanner.nextLine();

                System.out.println("Enter Password:");
                String password = scanner.nextLine();

                loggedInUser = loginService.login(username, password);

                if (loggedInUser != null) {

                    // تسجيل دخول كل دور
                    switch (loggedInUser.getRole().toLowerCase()) {
                        case "user":
                            loggedInUser = userService.findUserByName(loggedInUser.getName());
                            userService.login(loggedInUser.getName(), loggedInUser.getPassword());
                            break;

                        case "admin":
                            adminService.loginAdmin(loggedInUser);
                            break;

                        case "librarian":
                            librarianService.loginLibrarian(loggedInUser);
                            break;

                        default:
                            System.out.println("❌ Unknown role!");
                            loggedInUser = null;
                    }
                    if (loggedInUser != null) break; // خروج من اللوب عند تسجيل دخول ناجح
                } else {
                    System.out.println("❌ Wrong username or password! Try again.\n");
                }
            }

            // فتح القائمة المناسبة
            switch (loggedInUser.getRole().toLowerCase()) {
                case "admin":
                    adminMenu();
                    break;

                case "user":
                    userMenu(loggedInUser);
                    break;

                case "librarian":
                    librarianMenu();
                    break;
            }

            System.out.println("\n🔄 Returning to Login screen...\n");
        }
    }

    // -------------------- ADMIN MENU --------------------
    public static void adminMenu() {
        while (true) {
            System.out.println("\n===== ADMIN MENU =====");
            System.out.println("1. Add Book");
            System.out.println("2. Add CD");
            System.out.println("3. Search Media (Books + CDs)");
            System.out.println("4. Send Reminder Emails");
            System.out.println("5. Unregister User");
            System.out.println("6. View All Books & Overdue");
            System.out.println("7. Logout");
            System.out.println("======================");
            System.out.print("Enter your choice: ");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
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

    // -------------------- USER MENU --------------------
    public static void userMenu(User user) {
        while (true) {
            System.out.println("\n===== USER MENU =====");
            System.out.println("1. Search Book");
            System.out.println("2. Borrow Book");
            System.out.println("3. Pay Fine");
            System.out.println("4. Logout");
            System.out.println("======================");
            System.out.print("Enter choice: ");

            int choice = scanner.nextInt();
            scanner.nextLine();

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
                    double fine = userService.getFineFromFile(user.getName());
                    if (fine == 0) {
                        System.out.println("✅ You have no fines.");
                        break;
                    }
                    System.out.println("Your fine: " + fine);
                    System.out.print("Pay full or partial? (full/partial): ");
                    String type = scanner.nextLine();
                    double payAmount = type.equalsIgnoreCase("full") ? fine : scanner.nextDouble();
                    scanner.nextLine();
                    userService.payFine(user, payAmount);
                    break;
                case 4:
                    userService.logout();
                    return;
                default:
                    System.out.println("❌ Invalid choice.");
            }
        }
    }

    // -------------------- LIBRARIAN MENU --------------------
    public static void librarianMenu() {
        while (true) {
            System.out.println("\n===== LIBRARIAN MENU =====");
            System.out.println("1. Show Overdue Books");
            System.out.println("2. Issue Fines");
            System.out.println("3. Logout");
            System.out.println("===========================");
            System.out.print("Enter choice: ");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    librarianService.showOverdueBooks(bookService.getAllBooks());
                    break;
                case 2:
                    librarianService.issueFines(bookService.getAllBooks());
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
