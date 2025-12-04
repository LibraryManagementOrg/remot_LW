package model;

import java.time.LocalDate;

/**
 * Represents a Book.
 * Updated for Sprint 5 to extend Media and use Strategy Pattern.
 */
public class Book extends media { // ✅ تأكد أن Media مكتوبة بحرف كبير (Class Name)

    public Book(String title, String author, String isbn) {
        // نمرر البيانات للكلاس الأب Media
        // (title, creator, id) -> (title, author, isbn)
        super(title, author, isbn);
        
        // ✅ هذا السطر كان يعطي خطأ لأنك لم تنشئ كلاس BookFineStrategy بعد
        // الآن بعد إنشاء الملف في الخطوة 1، سيعمل هذا السطر بنجاح
        this.setFineStrategy(new BookFineStrategy());
    }

    // ==========================================
    // ✅ تنفيذ الدوال المطلوبة من الكلاس الأب
    // ==========================================
    
    @Override
    public int getLoanPeriod() {
        return 28; // Sprint 2 requirement
    }

    @Override
    public double getDailyFine() {
        return 10.0; // Sprint 5 requirement (Used if strategy fails)
    }

    // ==========================================
    // 🔄 دوال للحفاظ على عمل الكود القديم (Backward Compatibility)
    // ==========================================

    public String getAuthor() { return super.getCreator(); }
    public String getIsbn() { return super.getId(); }

    // ==========================================
    // ⚙️ المنطق (يستخدم دوال الأب)
    // ==========================================

    public void borrow(User user) {
        if (isBorrowed()) {
            throw new IllegalStateException("Book is already borrowed!");
        }
        setBorrowed(true);
        setDueDate(LocalDate.now().plusDays(getLoanPeriod())); 
        setBorrowedBy(user);
        setFineIssued(false);
    }

    public void returnBook() {
        setBorrowed(false);
        setDueDate(null);
        setBorrowedBy(null);
        setFineIssued(false);
    }

    @Override
    public double getFineAmount() {
        return super.getFineAmount(); 
    }

    // ==========================================
    // 💾 التعامل مع الملفات
    // ==========================================

    @Override
    public String toString() {
        return "Book: " + getTitle() + " | Author: " + getAuthor() + " | ISBN: " + getIsbn();
    }

    public String toFileString() {
        return "BOOK;" +
                getTitle() + ";" +
                getCreator() + ";" +
                getId() + ";" +
                isBorrowed() + ";" +
                (getDueDate() != null ? getDueDate() : "null") + ";" +
                (getBorrowedBy() != null ? getBorrowedBy().getName() : "null") + ";" +
                isFineIssued();
    }

    public static Book fromFileString(String line) {
        if (line == null || line.isBlank()) return null;
        String[] parts = line.split(";", -1);

        int offset = 0;
        if (parts[0].equalsIgnoreCase("BOOK")) {
            offset = 1;
        }

        if (parts.length < 3 + offset) return null;

        Book book = new Book(parts[0 + offset], parts[1 + offset], parts[2 + offset]);

        if (parts.length > 3 + offset)
            book.setBorrowed(Boolean.parseBoolean(parts[3 + offset]));

        if (parts.length > 4 + offset && !parts[4 + offset].equals("null"))
            book.setDueDate(LocalDate.parse(parts[4 + offset]));

        if (parts.length > 5 + offset && !parts[5 + offset].equals("null"))
            book.setBorrowedBy(new User(parts[5 + offset], "", "User")); 

        if (parts.length > 6 + offset)
            book.setFineIssued(Boolean.parseBoolean(parts[6 + offset]));

        return book;
    }
}