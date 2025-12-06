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
    
    private String filePath = "src/main/resources/books.txt"; 

    public BookService(AdminService adminService, UserService userService) {
        this.adminService = adminService;
        this.userService = userService;
        loadItemsFromFile();
    }

    public BookService(AdminService adminService, UserService userService, String testFilePath) {
        this.adminService = adminService;
        this.userService = userService;
        this.filePath = testFilePath;
        loadItemsFromFile();
    }

    private void loadItemsFromFile() {
        File file = new File(this.filePath);
        if (!file.exists()) {
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(this.filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                
                String[] parts = line.split(";", -1);
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

    public void saveBooksToFile() { 
        try (PrintWriter pw = new PrintWriter(new FileWriter(this.filePath))) {
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

    // 🔴 تم التعديل: يعيد String
    public String addBook(String title, String author, String isbn) {
        if (!adminService.isLoggedIn()) {
            return "❌ Access denied. Admin login required.";
        }

        if (findMediaById(isbn) != null) {
            return "⛔ Error: A media item with ID (ISBN) [" + isbn + "] already exists!";
        }

        items.add(new Book(title, author, isbn));
        saveBooksToFile();
        return "📗 Book added successfully!";
    }

    // 🔴 تم التعديل: يعيد String
    public String addCD(String title, String artist, String barcode) {
        if (!adminService.isLoggedIn()) {
            return "❌ Access denied. Admin login required.";
        }

        if (findMediaById(barcode) != null) {
            return "⛔ Error: A media item with ID (Barcode) [" + barcode + "] already exists!";
        }

        items.add(new CD(title, artist, barcode));
        saveBooksToFile();
        return "💿 CD added successfully!";
    }

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

    // 🔴 تم التعديل: يعيد String (بدلاً من boolean)
    public String borrowBook(User user, String id) {
        User realUser = userService.findUserByName(user.getName());

        if (realUser.getOutstandingFine() > 0) { 
            return "❌ You cannot borrow new items until you pay your fines.";
        }

        media item = findMediaById(id);
        if (item == null) {
            return "❌ Item not found.";
        }

        if (item.isBorrowed()) {
            return "❌ Item is already borrowed.";
        }

        item.setBorrowed(true);
        item.setBorrowedBy(realUser);
        item.setDueDate(LocalDate.now().plusDays(item.getLoanPeriod())); 
        item.setFineIssued(false);

        saveBooksToFile();

        return String.format("✅ Borrowed: %s | 📅 Due Date: %s (Loan Period: %d days)", 
                             item.getTitle(), item.getDueDate(), item.getLoanPeriod());
    }

    // 🔴 تم التعديل: يعيد String (بدلاً من void)
    public String returnBook(String id, User user) {
        media item = findMediaById(id);

        if (item == null) {
            return "❌ Item not found.";
        }

        if (!item.isBorrowed()) {
            return "⚠ Item already returned.";
        }

        if (item.getBorrowedBy() == null ||
            !item.getBorrowedBy().getName().equalsIgnoreCase(user.getName())) {
            return "❌ You cannot return an item borrowed by another user.";
        }

        // --- التحقق من التأخير وحساب الغرامة ---
        if (item.getDueDate() != null && LocalDate.now().isAfter(item.getDueDate())) {
            long daysOverdue = ChronoUnit.DAYS.between(item.getDueDate(), LocalDate.now());
            
            // حساب الغرامة
            double dailyFine = (item instanceof Book) ? 2.0 : 5.0; 
            double fineAmount = daysOverdue * dailyFine;

            // تم إزالة Scanner والطباعة هنا، ونعيد رسالة للـ mymain للتعامل معها
            return String.format("⚠ OVERDUE: This item is late by %d days. Total Fine: $%.2f. Cannot return without payment.", 
                                 daysOverdue, fineAmount);
        }

        // --- إتمام عملية الإرجاع ---
        item.setBorrowed(false);
        item.setDueDate(null);
        item.setBorrowedBy(null);
        item.setFineIssued(false);
        
        saveBooksToFile();
        
        String typeEmoji = (item instanceof Book) ? "📘" : "💿";
        return typeEmoji + " Item returned successfully and is now AVAILABLE!";
    }

    public media findMediaById(String id) {
        for (media m : items) {
            if (m.getId().equalsIgnoreCase(id)) return m;
        }
        return null;
    }

    public List<media> getAllBooks() {
        return items;
    }

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