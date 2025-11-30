package service;

import model.Book;
import model.User;

import java.io.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class BookService {

    private List<Book> books = new ArrayList<>();
    private AdminService adminService;
    private UserService userService; // للوصول إلى المستخدم الحقيقي
    private final String FILE_PATH = "src/main/resources/books.txt";

    // 🔹 Constructor — تحميل الكتب من الملف عند التشغيل
    public BookService(AdminService adminService, UserService userService) {
        this.adminService = adminService;
        this.userService = userService;
        loadBooksFromFile();
    }

    // 🔹 تحميل الكتب من books.txt
    private void loadBooksFromFile() {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            System.out.println("📂 No books file found. A new file will be created when adding books...");
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                try {
                    Book b = Book.fromFileString(line);
                    books.add(b);
                } catch (Exception e) {
                    System.out.println("⚠ Skipping corrupted line: " + line);
                }
            }
        } catch (Exception e) {
            System.out.println("❌ Error loading books: " + e.getMessage());
        }
    }

    // 🔹 حفظ كل الكتب إلى books.txt
    private void saveBooksToFile() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE_PATH))) {
            for (Book b : books) {
                pw.println(b.toFileString());
            }
        } catch (IOException e) {
            System.out.println("❌ Error saving books: " + e.getMessage());
        }
    }

    // 🔹 إضافة كتاب
    public void addBook(String title, String author, String isbn) {
        if (!adminService.isLoggedIn()) {
            System.out.println("❌ Access denied. Admin login required.");
            return;
        }

        if (findBookByISBN(isbn) != null) {
            System.out.println("⚠ A book with this ISBN already exists.");
            return;
        }

        Book b = new Book(title, author, isbn);
        books.add(b);

        // حفظ الكتاب الجديد مباشرة في الملف
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE_PATH, true))) {
            pw.println(b.toFileString());
        } catch (Exception e) {
            System.out.println("❌ Error saving book to file: " + e.getMessage());
            return;
        }

        System.out.println("📗 Book added successfully!");
    }

    // 🔹 البحث عن كتاب وعرض النتائج مباشرة
    public void searchBook(String keyword) {
        List<Book> results = new ArrayList<>();
        for (Book b : books) {
            if (b.getTitle().toLowerCase().contains(keyword.toLowerCase()) ||
                b.getAuthor().toLowerCase().contains(keyword.toLowerCase()) ||
                b.getIsbn().equalsIgnoreCase(keyword)) {
                results.add(b);
            }
        }

        if (results.isEmpty()) {
            System.out.println("❌ No books found matching \"" + keyword + "\"");
        } else {
            System.out.println("🔍 Search results for \"" + keyword + "\":");
            for (Book b : results) {
                System.out.println(b);  // يستخدم toString لطباعة الكتاب
            }
        }
    }

    // 🔹 استعارة كتاب (ربطه بالمستخدم الحقيقي من UserService)
    public boolean borrowBook(User user, String isbn) {
        if (!user.canBorrow()) {
            System.out.println("❌ You have unpaid fines.");
            return false;
        }

        Book b = findBookByISBN(isbn);
        if (b == null) {
            System.out.println("❌ Book not found.");
            return false;
        }
        if (b.isBorrowed()) {
            System.out.println("❌ Book already borrowed.");
            return false;
        }

        // نستخدم الكائن الحقيقي من UserService
        User realUser = userService.findUserByName(user.getName());
        if (realUser == null) {
            System.out.println("❌ User not found in system.");
            return false;
        }

        b.borrow(realUser);  // sets isBorrowed = true, dueDate = now + 28 days, stores user
        saveBooksToFile();   // حفظ التغيير في الملف
        System.out.println("✅ Borrowed: " + b.getTitle() + " | Due: " + b.getDueDate());
        return true;
    }

    // 🔹 إرجاع كتاب
    public void returnBook(String isbn, User user) {
        Book b = findBookByISBN(isbn);
        if (b == null) {
            System.out.println("❌ Book not found.");
            return;
        }

        if (!b.isBorrowed()) {
            System.out.println("⚠ Book already returned.");
            return;
        }

        if (b.isOverdue()) {
            long daysLate = ChronoUnit.DAYS.between(b.getDueDate(), LocalDate.now());
            double fine = daysLate * 1.0;

            // نضيف الغرامة للكائن الحقيقي من UserService
            User realUser = userService.findUserByName(user.getName());
            if (realUser != null) {
                realUser.addFine(fine);
            }

            System.out.println("⚠ Overdue! Fine added: " + fine);
        }

        b.returnBook();
        saveBooksToFile(); // تحديث الملف بعد الإرجاع
        System.out.println("📘 Book returned successfully!");
    }

    // 🔹 البحث بالـ ISBN
    private Book findBookByISBN(String isbn) {
        for (Book b : books) {
            if (b.getIsbn().equalsIgnoreCase(isbn)) {
                return b;
            }
        }
        return null;
    }

    // 🔹 عرض كل الكتب
    public List<Book> getAllBooks() {
        return books;
    }

    // 🔹 For testing: Make a borrowed book overdue
    public void makeBookOverdue(String isbn, int daysOverdue) {
        Book b = findBookByISBN(isbn);
        if (b != null && b.isBorrowed()) {
            b.setDueDate(LocalDate.now().minusDays(daysOverdue));
            System.out.println("Book " + b.getTitle() + " is now overdue by " + daysOverdue + " days.");
        } else {
            System.out.println("Book not found or not borrowed.");
        }
    }

}
