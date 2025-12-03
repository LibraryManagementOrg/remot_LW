package service;

import model.Book;
import model.CD;
import model.media;
import model.User;
import java.io.*;
<<<<<<< HEAD
=======
import java.time.LocalDate;
>>>>>>> branch 'master' of https://github.com/layalqaradeh/remot_LW.git
import java.util.ArrayList;
import java.util.List;

public class BookService {
<<<<<<< HEAD
    
    private List<Book> books = new ArrayList<>();
    private final String FILE_PATH = "src/main/resources/books.txt"; // مسار الملف
=======

    // 🌟 تغيير القائمة لتستوعب Media (كتب + CDs)
    private List<media> items = new ArrayList<>();
>>>>>>> branch 'master' of https://github.com/layalqaradeh/remot_LW.git
    
    private AdminService adminService;
    private UserService userService;
<<<<<<< HEAD
=======
    private final String FILE_PATH = "src/main/resources/books.txt";
>>>>>>> branch 'master' of https://github.com/layalqaradeh/remot_LW.git

    public BookService(AdminService adminService, UserService userService) {
        this.adminService = adminService;
        this.userService = userService;
<<<<<<< HEAD
        loadBooksFromFile(); // ✅ قراءة الكتب القديمة عند التشغيل
=======
        loadItemsFromFile();
>>>>>>> branch 'master' of https://github.com/layalqaradeh/remot_LW.git
    }

<<<<<<< HEAD
    // ==========================================
    //           ADD BOOK (مع الحفظ)
    // ==========================================
=======
    // =============================
    //      تحميل الوسائط من الملف
    // =============================
    private void loadItemsFromFile() {
        File file = new File(FILE_PATH);
        if (!file.exists()) {
            System.out.println("📂 No library data file found. A new one will be created.");
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                
                String[] parts = line.split(";", -1);
                // التنسيق: TYPE;Title;Creator;ID;IsBorrowed;DueDate;User;FineIssued
                if (parts.length < 4) continue;

                String type = parts[0];
                String title = parts[1];
                String creator = parts[2];
                String id = parts[3];

                media item = null;

                // 🌟 التمييز بين الكتاب والسي دي عند التحميل
                if (type.equalsIgnoreCase("BOOK")) {
                    item = new Book(title, creator, id);
                } else if (type.equalsIgnoreCase("CD")) {
                    item = new CD(title, creator, id);
                }

                if (item != null) {
                    // استعادة الحالة (مستعار أم لا)
                    if (parts.length > 4) item.setBorrowed(Boolean.parseBoolean(parts[4]));
                    if (parts.length > 5 && !parts[5].equals("null")) item.setDueDate(LocalDate.parse(parts[5]));
                    
                    // استعادة المستخدم المستعير
                    if (parts.length > 6 && !parts[6].equals("null")) {
                        // نحاول ربطه بمستخدم حقيقي من UserService
                        User u = userService.findUserByName(parts[6]);
                        if (u == null) u = new User(parts[6], "", "User"); // Fallback
                        item.setBorrowedBy(u);
                    }
                    
                    if (parts.length > 7) item.setFineIssued(Boolean.parseBoolean(parts[7]));

                    items.add(item);
                }
            }
        } catch (Exception e) {
            System.out.println("❌ Error loading library items: " + e.getMessage());
        }
    }

    // =============================
    //        حفظ الوسائط للملف
    // =============================
    public void saveBooksToFile() { // الاسم بقي كما هو لعدم كسر الكود في أماكن أخرى
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE_PATH))) {
            for (media m : items) {
                // تحديد النوع للحفظ
                String type = (m instanceof CD) ? "CD" : "BOOK";
                String user = (m.getBorrowedBy() != null) ? m.getBorrowedBy().getName() : "null";
                String date = (m.getDueDate() != null) ? m.getDueDate().toString() : "null";

                // كتابة السطر
                pw.println(type + ";" +
                           m.getTitle() + ";" +
                           m.getCreator() + ";" +
                           m.getId() + ";" +
                           m.isBorrowed() + ";" +
                           date + ";" +
                           user + ";" +
                           m.isFineIssued());
            }
        } catch (IOException e) {
            System.out.println("❌ Error saving library items: " + e.getMessage());
        }
    }

    // =============================
    //           إضافة كتاب
    // =============================
>>>>>>> branch 'master' of https://github.com/layalqaradeh/remot_LW.git
    public void addBook(String title, String author, String isbn) {
<<<<<<< HEAD
        Book newBook = new Book(title, author, isbn);
        books.add(newBook);
        saveBooksToFile(); // ✅ حفظ فوري في الملف
        System.out.println("✅ Book added and saved to file successfully!");
=======
        if (!adminService.isLoggedIn()) {
            System.out.println("❌ Access denied. Admin login required.");
            return;
        }

        if (findMediaById(isbn) != null) {
            System.out.println("⚠ An item with this ID already exists.");
            return;
        }

        items.add(new Book(title, author, isbn));
        saveBooksToFile();
        System.out.println("📗 Book added successfully!");
>>>>>>> branch 'master' of https://github.com/layalqaradeh/remot_LW.git
    }

<<<<<<< HEAD
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
=======
    // =============================
    //        إضافة CD (جديد)
    // =============================
    public void addCD(String title, String artist, String barcode) {
        if (!adminService.isLoggedIn()) {
            System.out.println("❌ Access denied. Admin login required.");
            return;
        }

        if (findMediaById(barcode) != null) {
            System.out.println("⚠ An item with this ID already exists.");
            return;
        }

        items.add(new CD(title, artist, barcode));
        saveBooksToFile();
        System.out.println("💿 CD added successfully!");
    }

    // =============================
    //         البحث (شامل)
    // =============================
    public void searchBook(String keyword) {
        List<media> results = new ArrayList<>();

        for (media m : items) {
            if (m.getTitle().toLowerCase().contains(keyword.toLowerCase()) ||
                m.getCreator().toLowerCase().contains(keyword.toLowerCase()) ||
                m.getId().equalsIgnoreCase(keyword)) {

                results.add(m);
>>>>>>> branch 'master' of https://github.com/layalqaradeh/remot_LW.git
            }
        }
<<<<<<< HEAD
        System.out.println("❌ Book not found.");
=======

        if (results.isEmpty()) {
            System.out.println("❌ No items found matching \"" + keyword + "\"");
        } else {
            System.out.println("🔍 Search results:");
            for (media m : results) {
                System.out.println(m); // سيستخدم toString الخاص بـ Book أو CD
            }
        }
>>>>>>> branch 'master' of https://github.com/layalqaradeh/remot_LW.git
    }

<<<<<<< HEAD
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
=======
    // =============================
    //        استعارة (Polymorphic)
    // =============================
    public boolean borrowBook(User user, String id) {
        User realUser = userService.findUserByName(user.getName());

        if (!realUser.canBorrow()) {
            System.out.println("❌ You cannot borrow new items until you pay your fines.");
            return false;
        }

        media item = findMediaById(id);
        if (item == null) {
            System.out.println("❌ Item not found.");
            return false;
        }

        if (item.isBorrowed()) {
            System.out.println("❌ Item is already borrowed.");
            return false;
        }

        // 🌟 Polymorphism: getLoanPeriod() will return 28 for Book, 7 for CD
        item.setBorrowed(true);
        item.setBorrowedBy(realUser);
        item.setDueDate(LocalDate.now().plusDays(item.getLoanPeriod())); 
        item.setFineIssued(false);

        saveBooksToFile();

        System.out.println("✅ Borrowed: " + item.getTitle());
        System.out.println("📅 Due Date: " + item.getDueDate() + " (Loan Period: " + item.getLoanPeriod() + " days)");
        return true;
    }

    // =============================
    //         إرجاع (Polymorphic)
    // =============================
    public void returnBook(String id, User user) {
        media item = findMediaById(id);

        if (item == null) {
            System.out.println("❌ Item not found.");
            return;
        }

        if (!item.isBorrowed()) {
            System.out.println("⚠ Item already returned.");
            return;
        }

        if (item.getBorrowedBy() == null ||
            !item.getBorrowedBy().getName().equalsIgnoreCase(user.getName())) {
            System.out.println("❌ You cannot return an item borrowed by another user.");
            return;
        }

        // إرجاع العنصر
        item.setBorrowed(false);
        item.setDueDate(null);
        item.setBorrowedBy(null);
        item.setFineIssued(false);
        
        saveBooksToFile();
        System.out.println("📘 Item returned successfully!");
    }

    // =============================
    //     البحث عبر ID (ISBN/Barcode)
    // =============================
    public media findMediaById(String id) {
        for (media m : items) {
            if (m.getId().equalsIgnoreCase(id)) return m;
>>>>>>> branch 'master' of https://github.com/layalqaradeh/remot_LW.git
        }
        System.out.println("❌ Cannot return book (Not found or not borrowed).");
    }

<<<<<<< HEAD
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
=======
    // =============================
    //     عرض كل الوسائط
    // =============================
    public List<media> getAllBooks() {
        return items;
>>>>>>> branch 'master' of https://github.com/layalqaradeh/remot_LW.git
    }

<<<<<<< HEAD
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
=======
    // =============================
    //   جعل عنصر متأخر (للتجربة)
    // =============================
    public void makeBookOverdue(String id, int days) {
        media m = findMediaById(id);
        if (m != null && m.isBorrowed()) {
            m.setDueDate(LocalDate.now().minusDays(days));
            System.out.println("Item " + m.getTitle() + " is now overdue.");
        } else {
            System.out.println("Item not found or not borrowed.");
>>>>>>> branch 'master' of https://github.com/layalqaradeh/remot_LW.git
        }
    }
<<<<<<< HEAD

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
=======
>>>>>>> branch 'master' of https://github.com/layalqaradeh/remot_LW.git
}