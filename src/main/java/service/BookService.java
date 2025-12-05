package service;

import model.Book;
import model.CD;
import model.media;
import model.User;

import java.io.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class BookService {

    private List<media> items = new ArrayList<>();
    
    private AdminService adminService;
    private UserService userService;
    
    // ✅ جعلنا المسار متغيراً وليس final ليمكن تغييره في الاختبارات
    private String filePath = "src/main/resources/books.txt"; 

    // =============================================================
    // 1️⃣ الكونستركتور الافتراضي (للبرنامج الرئيسي)
    // =============================================================
    public BookService(AdminService adminService, UserService userService) {
        this.adminService = adminService;
        this.userService = userService;
        // يستخدم المسار الافتراضي (books.txt)
        loadItemsFromFile();
    }

    // =============================================================
    // 2️⃣ كونستركتور مخصص للاختبارات (Test Constructor)
    // ✅ يسمح بتمرير مسار ملف وهمي لكي لا نعدل الملف الأصلي
    // =============================================================
    public BookService(AdminService adminService, UserService userService, String testFilePath) {
        this.adminService = adminService;
        this.userService = userService;
        this.filePath = testFilePath; // استخدام الملف الوهمي
        loadItemsFromFile();
    }

    // =============================
    //      تحميل الوسائط من الملف
    // =============================
    private void loadItemsFromFile() {
        File file = new File(this.filePath); // ✅ استخدام المتغير
        if (!file.exists()) {
            // لا نطبع رسالة خطأ هنا لأن إنشاء ملف جديد أمر طبيعي في البداية
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(this.filePath))) { // ✅ استخدام المتغير
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

                if (type.equalsIgnoreCase("BOOK")) {
                    item = new Book(title, creator, id);
                } else if (type.equalsIgnoreCase("CD")) {
                    item = new CD(title, creator, id);
                }

                if (item != null) {
                    if (parts.length > 4) item.setBorrowed(Boolean.parseBoolean(parts[4]));
                    if (parts.length > 5 && !parts[5].equals("null")) item.setDueDate(LocalDate.parse(parts[5]));
                    
                    if (parts.length > 6 && !parts[6].equals("null")) {
                        User u = userService.findUserByName(parts[6]);
                        if (u == null) u = new User(parts[6], "", "User");
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
    public void saveBooksToFile() { 
        try (PrintWriter pw = new PrintWriter(new FileWriter(this.filePath))) { // ✅ استخدام المتغير
            for (media m : items) {
                String type = (m instanceof CD) ? "CD" : "BOOK";
                String user = (m.getBorrowedBy() != null) ? m.getBorrowedBy().getName() : "null";
                String date = (m.getDueDate() != null) ? m.getDueDate().toString() : "null";

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

        // ✅ التحقق من الرقم الفريد
        if (findMediaById(isbn) != null) {
            System.out.println("⛔ Error: A media item with ID (ISBN) [" + isbn + "] already exists!");
            return;
        }

        items.add(new Book(title, author, isbn));
        saveBooksToFile();
        System.out.println("📗 Book added successfully!");
    }

    // =============================
    //        إضافة CD
    // =============================
    public void addCD(String title, String artist, String barcode) {
        if (!adminService.isLoggedIn()) {
            System.out.println("❌ Access denied. Admin login required.");
            return;
        }

        // ✅ التحقق من الرقم الفريد
        if (findMediaById(barcode) != null) {
            System.out.println("⛔ Error: A media item with ID (Barcode) [" + barcode + "] already exists!");
            return;
        }

        items.add(new CD(title, artist, barcode));
        saveBooksToFile();
        System.out.println("💿 CD added successfully!");
    }

    // =============================
    //         البحث
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
                System.out.println(m);
            }
        }
    }

    // =============================
    //        استعارة
    // =============================
    public boolean borrowBook(User user, String id) {
        User realUser = userService.findUserByName(user.getName());

        // ✅ منع الاستعارة في حال وجود غرامات
        if (realUser.getOutstandingFine() > 0) { 
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
    //         إرجاع (مع الدفع الفوري)
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

        // --- التحقق من التأخير وحساب الغرامة ---
        double fineAmount = 0.0;
        if (item.getDueDate() != null && LocalDate.now().isAfter(item.getDueDate())) {
            long daysOverdue = ChronoUnit.DAYS.between(item.getDueDate(), LocalDate.now());
            
            // حساب الغرامة (2 للكتاب، 5 للسي دي)
            double dailyFine = (item instanceof Book) ? 2.0 : 5.0; 
            fineAmount = daysOverdue * dailyFine;

            System.out.println("⚠ ALERT: This item is OVERDUE by " + daysOverdue + " days.");
            System.out.println("💲 Total Fine required to return: $" + fineAmount);
            System.out.println("🛑 You cannot return this item without paying the fine.");
            
            System.out.print("Do you want to pay now and return the item? (yes/no): ");
            Scanner scanner = new Scanner(System.in); 
            String choice = scanner.next();

            if (!choice.equalsIgnoreCase("yes")) {
                System.out.println("❌ Return cancelled. You must pay to return the item.");
                return; // 🛑 إيقاف العملية
            }

            System.out.println("💸 Processing payment of $" + fineAmount + "...");
            System.out.println("✅ Payment Successful!");
            // لا نضيف الغرامة لحساب المستخدم لأنه دفعها فوراً
        }

        // --- إتمام عملية الإرجاع ---
        item.setBorrowed(false);
        item.setDueDate(null);
        item.setBorrowedBy(null);
        item.setFineIssued(false);
        
        saveBooksToFile();
        
        String typeEmoji = (item instanceof Book) ? "📘" : "💿";
        System.out.println(typeEmoji + " Item returned successfully and is now AVAILABLE!");
    }

    // =============================
    //     البحث عبر ID
    // =============================
    public media findMediaById(String id) {
        for (media m : items) {
            if (m.getId().equalsIgnoreCase(id)) return m;
        }
        return null;
    }

    public List<media> getAllBooks() {
        return items;
    }

    // =============================
    //   جعل عنصر متأخر (للاختبار)
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