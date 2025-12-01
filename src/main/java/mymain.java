import service.*;
import model.*;

import java.util.Scanner;

public class mymain {

    static Scanner scanner = new Scanner(System.in);

    // ===== Shared Services =====
    static AdminService adminService = new AdminService();
    static UserService userService = new UserService();
    
    // نمرر الخدمات لبعضها البعض حسب الحاجة
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
            System.out.println("2. Add CD"); // ✅ مفعل الآن
            System.out.println("3. Search Media");
            System.out.println("4. Send Reminder Emails");
            System.out.println("5. Unregister User");
            System.out.println("6. View All Media (Books & CDs)");
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
                    // ✅ US5.1: إضافة CD
                    if (!adminService.isLoggedIn()) {
                        System.out.println("⚠ Please log in as Admin first!");
                        break;
                    }
                    System.out.print("Enter CD Title: ");
                    String cdTitle = scanner.nextLine();
                    System.out.print("Enter Artist: ");
                    String artist = scanner.nextLine();
                    System.out.print("Enter Barcode: ");
                    String barcode = scanner.nextLine();
                    bookService.addCD(cdTitle, artist, barcode);
                    break;

                case 3:
                    System.out.print("Enter search keyword: ");
                    String keyword = scanner.nextLine();
                    bookService.searchBook(keyword);
                    break;

                case 4:
                    // ✅ تفعيل إرسال الإيميلات
                    // ملاحظة: تأكد أن AdminService تم تحديثه ليقبل List<Media> كما شرحنا سابقاً
                	//adminService.sendOverdueReminders(userService.getAllUsers(), bookService.getAllBooks());
                    break;

                case 5:
                    // ✅ تفعيل حذف المستخدم
                    System.out.println("\n=== Unregister User ===");
                    System.out.print("Enter username to delete: ");
                    String userToDelete = scanner.nextLine();
                    
                    // استدعاء الدالة من AdminService للتحقق والحذف
                    adminService.unregisterUser(userToDelete, userService, bookService);
                    break;

                case 6:
                    // ✅ تعديل العرض ليشمل Media بدلاً من Book فقط
                    System.out.println("📚 All Media Status:");
                    boolean hasItems = false;
                    
                    // نستخدم Media لأنه الأب المشترك للكتب والسيديات
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
                        // Polymorphism: m.toString() will behave differently for Book vs CD
                        System.out.println(m.toString() + " | " + status);
                    }
                    if (!hasItems) System.out.println("No items in library.");
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
            System.out.println("1. Search Media");
            System.out.println("2. Borrow Item (Book/CD)");
            System.out.println("3. Return Item");
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
                        System.out.println("❌ You cannot borrow items until you pay your fines. Outstanding fine: " + user.getOutstandingFine());
                        break;
                    }
                    System.out.print("Enter ISBN (Book) or Barcode (CD) to borrow: ");
                    String id = scanner.nextLine();
                    bookService.borrowBook(user, id); // الدالة الآن تدعم الاثنين
                    break;

                case 3:
                    if (user.getOutstandingFine() > 0) {
                        System.out.println("❌ You cannot return items until you pay your fines (Logic from previous sprint). Outstanding fine: " + user.getOutstandingFine());
                        break;
                    }
                    System.out.print("Enter ISBN or Barcode to return: ");
                    String returnId = scanner.nextLine();
                    bookService.returnBook(returnId, user);
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
            System.out.println("1. Show Overdue Items");
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