package service;

import model.Book;
import model.CD;
import model.media;
import model.User;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BookService {

    // 🌟 تغيير القائمة لتستوعب Media (كتب + CDs)
    private List<media> items = new ArrayList<>();
    
    private AdminService adminService;
    private UserService userService;
    private final String FILE_PATH = "src/main/resources/books.txt";

    public BookService(AdminService adminService, UserService userService) {
        this.adminService = adminService;
        this.userService = userService;
        loadItemsFromFile();
    }

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
    public void addBook(String title, String author, String isbn) {
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
    }

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
            }
        }

        if (results.isEmpty()) {
            System.out.println("❌ No items found matching \"" + keyword + "\"");
        } else {
            System.out.println("🔍 Search results:");
            for (media m : results) {
                System.out.println(m); // سيستخدم toString الخاص بـ Book أو CD
            }
        }
    }

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
        }
        return null;
    }

    // =============================
    //     عرض كل الوسائط
    // =============================
    public List<media> getAllBooks() {
        return items;
    }

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
        }
    }
}