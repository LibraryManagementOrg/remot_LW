package service;

import model.Book;
import model.User;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class BookService {
    
    private List<Book> books = new ArrayList<>();
    private final String FILE_PATH = "src/main/resources/books.txt"; // مسار الملف
    
    private AdminService adminService;
    private UserService userService;

    public BookService(AdminService adminService, UserService userService) {
        this.adminService = adminService;
        this.userService = userService;
        loadBooksFromFile(); // ✅ قراءة الكتب القديمة عند التشغيل
    }

    // ==========================================
    //           ADD BOOK (مع الحفظ)
    // ==========================================
    public void addBook(String title, String author, String isbn) {
        Book newBook = new Book(title, author, isbn);
        books.add(newBook);
        saveBooksToFile(); // ✅ حفظ فوري في الملف
        System.out.println("✅ Book added and saved to file successfully!");
    }

    // ==========================================
    //           BORROW & RETURN (مع الحفظ)
    // ==========================================
    public void borrowBook(User user, String isbn) {
        for (Book b : books) {
            if (b.getIsbn().equals(isbn)) {
                if (!b.isBorrowed()) {
                    b.borrow(user);
                    saveBooksToFile(); // ✅ تحديث الملف (لأن الحالة تغيرت)
                    System.out.println("✅ You borrowed: " + b.getTitle());
                } else {
                    System.out.println("❌ Book is already borrowed.");
                }
                return;
            }
        }
        System.out.println("❌ Book not found.");
    }

    public void returnBook(String isbn, User user) {
        for (Book b : books) {
            if (b.getIsbn().equals(isbn) && b.isBorrowed()) {
                // التحقق: هل هذا المستخدم هو من استعار الكتاب؟
                if (b.getBorrowedBy() != null && b.getBorrowedBy().getName().equals(user.getName())) {
                    b.returnBook();
                    saveBooksToFile(); // ✅ تحديث الملف
                    System.out.println("✅ Book returned successfully.");
                } else {
                    System.out.println("❌ You cannot return a book you didn't borrow!");
                }
                return;
            }
        }
        System.out.println("❌ Cannot return book (Not found or not borrowed).");
    }

    // ==========================================
    //           SEARCH & GET
    // ==========================================
    public void searchBook(String keyword) {
        boolean found = false;
        for (Book b : books) {
            if (b.getTitle().toLowerCase().contains(keyword.toLowerCase()) || 
                b.getIsbn().equals(keyword)) {
                System.out.println(b);
                found = true;
            }
        }
        if (!found) System.out.println("❌ No books found.");
    }

    public List<Book> getAllBooks() {
        return books;
    }

    // ==========================================
    //           FILE HANDLING (القراءة والكتابة)
    // ==========================================
    
    private void loadBooksFromFile() {
        books.clear();
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = br.readLine()) != null) {
                // نستخدم دالة fromFileString الموجودة في كلاس Book
                Book b = Book.fromFileString(line);
                books.add(b);
            }
        } catch (IOException e) {
            // الملف قد يكون فارغاً في البداية، لا مشكلة
        }
    }

    private void saveBooksToFile() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (Book b : books) {
                // نستخدم دالة toFileString الموجودة في كلاس Book
                bw.write(b.toFileString());
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("❌ Error saving books to file!");
        }
    }
}