package model;

import java.time.LocalDate;

<<<<<<< HEAD
public class Book {

    private String title;
    private String author;
    private String isbn;
    private boolean isBorrowed;
    private LocalDate dueDate;
    private User borrowedBy;
    private boolean fineIssued;
=======
/**
 * Represents a Book.
 * Updated for Sprint 5 to extend Media and use Strategy Pattern.
 */
public class Book extends media { // ✅ تأكد أن Media مكتوبة بحرف كبير (Class Name)
>>>>>>> branch 'master' of https://github.com/layalqaradeh/remot_LW.git

    public Book(String title, String author, String isbn) {
        // نمرر البيانات للكلاس الأب Media
        // (title, creator, id) -> (title, author, isbn)
        super(title, author, isbn);
        
        // ✅ هذا السطر كان يعطي خطأ لأنك لم تنشئ كلاس BookFineStrategy بعد
        // الآن بعد إنشاء الملف في الخطوة 1، سيعمل هذا السطر بنجاح
        this.setFineStrategy(new BookFineStrategy());
    }

<<<<<<< HEAD
    // Getters & Setters
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getIsbn() { return isbn; }
    public boolean isBorrowed() { return isBorrowed; }
    public LocalDate getDueDate() { return dueDate; }
    public User getBorrowedBy() { return borrowedBy; }
    public boolean isFineIssued() { return fineIssued; }
=======
    // ==========================================
    // ✅ تنفيذ الدوال المطلوبة من الكلاس الأب
    // ==========================================
    
    @Override
    public int getLoanPeriod() {
        return 28; // Sprint 2 requirement
    }
>>>>>>> branch 'master' of https://github.com/layalqaradeh/remot_LW.git

<<<<<<< HEAD
    public void setBorrowed(boolean borrowed) { this.isBorrowed = borrowed; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public void setBorrowedBy(User user) { this.borrowedBy = user; }
    public void setFineIssued(boolean fineIssued) { this.fineIssued = fineIssued; }
=======
    @Override
    public double getDailyFine() {
        return 10.0; // Sprint 5 requirement (Used if strategy fails)
    }
>>>>>>> branch 'master' of https://github.com/layalqaradeh/remot_LW.git

<<<<<<< HEAD
    // Logic
=======
    // ==========================================
    // 🔄 دوال للحفاظ على عمل الكود القديم (Backward Compatibility)
    // ==========================================

    public String getAuthor() { return super.getCreator(); }
    public String getIsbn() { return super.getId(); }

    // ==========================================
    // ⚙️ المنطق (يستخدم دوال الأب)
    // ==========================================

>>>>>>> branch 'master' of https://github.com/layalqaradeh/remot_LW.git
    public void borrow(User user) {
<<<<<<< HEAD
        this.isBorrowed = true;
        this.dueDate = LocalDate.now().plusDays(28);
        this.borrowedBy = user;
=======
        if (isBorrowed()) {
            throw new IllegalStateException("Book is already borrowed!");
        }
        setBorrowed(true);
        setDueDate(LocalDate.now().plusDays(getLoanPeriod())); 
        setBorrowedBy(user);
        setFineIssued(false);
>>>>>>> branch 'master' of https://github.com/layalqaradeh/remot_LW.git
    }

    public void returnBook() {
<<<<<<< HEAD
        this.isBorrowed = false;
        this.dueDate = null;
        this.borrowedBy = null;
=======
        setBorrowed(false);
        setDueDate(null);
        setBorrowedBy(null);
        setFineIssued(false);
>>>>>>> branch 'master' of https://github.com/layalqaradeh/remot_LW.git
    }

<<<<<<< HEAD
    public boolean isOverdue() {
        return isBorrowed && dueDate != null && dueDate.isBefore(LocalDate.now());
=======
    @Override
    public double getFineAmount() {
        return super.getFineAmount(); 
>>>>>>> branch 'master' of https://github.com/layalqaradeh/remot_LW.git
    }

<<<<<<< HEAD
    // ✅✅✅ هذه الدالة المسؤولة عن الكتابة داخل الملف (تأكدي أنها موجودة)
=======
    // ==========================================
    // 💾 التعامل مع الملفات
    // ==========================================

    @Override
    public String toString() {
        return "Book: " + getTitle() + " | Author: " + getAuthor() + " | ISBN: " + getIsbn();
    }

>>>>>>> branch 'master' of https://github.com/layalqaradeh/remot_LW.git
    public String toFileString() {
<<<<<<< HEAD
        String borrowerName = (borrowedBy != null) ? borrowedBy.getName() : "null";
        String dateStr = (dueDate != null) ? dueDate.toString() : "null";
        
        // الترتيب: Title;Author;ISBN;isBorrowed;DueDate;BorrowerName;FineIssued
        return title + ";" + author + ";" + isbn + ";" + isBorrowed + ";" + dateStr + ";" + borrowerName + ";" + fineIssued;
=======
        return "BOOK;" +
                getTitle() + ";" +
                getCreator() + ";" +
                getId() + ";" +
                isBorrowed() + ";" +
                (getDueDate() != null ? getDueDate() : "null") + ";" +
                (getBorrowedBy() != null ? getBorrowedBy().getName() : "null") + ";" +
                isFineIssued();
>>>>>>> branch 'master' of https://github.com/layalqaradeh/remot_LW.git
    }

<<<<<<< HEAD
    // ✅✅✅ هذه الدالة المسؤولة عن القراءة من الملف
=======
>>>>>>> branch 'master' of https://github.com/layalqaradeh/remot_LW.git
    public static Book fromFileString(String line) {
        if (line == null || line.isBlank()) return null;
        String[] parts = line.split(";", -1);
<<<<<<< HEAD
        if (parts.length < 3) return null;

        Book book = new Book(parts[0], parts[1], parts[2]);
=======
>>>>>>> branch 'master' of https://github.com/layalqaradeh/remot_LW.git

<<<<<<< HEAD
        if (parts.length > 3) book.setBorrowed(Boolean.parseBoolean(parts[3]));
        
        if (parts.length > 4 && !parts[4].equals("null")) {
            book.setDueDate(LocalDate.parse(parts[4]));
=======
        int offset = 0;
        if (parts[0].equalsIgnoreCase("BOOK")) {
            offset = 1;
>>>>>>> branch 'master' of https://github.com/layalqaradeh/remot_LW.git
        }

<<<<<<< HEAD
        if (parts.length > 5 && !parts[5].equals("null")) {
            // هنا ننشئ يوزر مؤقت يحمل الاسم فقط
            User u = new User(parts[5], "", "", "User");
            book.setBorrowedBy(u);
        }
        
        if (parts.length > 6) {
             book.setFineIssued(Boolean.parseBoolean(parts[6]));
        }
=======
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
>>>>>>> branch 'master' of https://github.com/layalqaradeh/remot_LW.git

        return book;
    }
<<<<<<< HEAD

    @Override
    public String toString() {
        return title + " by " + author + " (ISBN: " + isbn + ")";
    }
=======
>>>>>>> branch 'master' of https://github.com/layalqaradeh/remot_LW.git
}