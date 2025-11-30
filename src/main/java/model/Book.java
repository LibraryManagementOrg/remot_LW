package model;

import java.time.LocalDate;

public class Book {

    private String title;
    private String author;
    private String isbn;

    private boolean isBorrowed;
    private LocalDate dueDate;
    private User borrowedBy; // المستخدم الذي استعار الكتاب

    public Book(String title, String author, String isbn) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.isBorrowed = false;
        this.dueDate = null;
        this.borrowedBy = null;
    }

    // ===== Getters =====
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getIsbn() { return isbn; }
    public boolean isBorrowed() { return isBorrowed; }
    public LocalDate getDueDate() { return dueDate; }
    public User getBorrowedBy() { return borrowedBy; }

    // ===== Setters =====
    public void setBorrowed(boolean borrowed) { this.isBorrowed = borrowed; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public void setBorrowedBy(User user) { this.borrowedBy = user; }

    // ====================================================
    //              🔹 استعارة الكتاب
    // ====================================================
    public void borrow(User user) {
        if (isBorrowed) {
            throw new IllegalStateException("Book is already borrowed!");
        }
        this.isBorrowed = true;
        this.dueDate = LocalDate.now().plusDays(28);
        this.borrowedBy = user; // نخزن اسم المستخدم
    }

    // ====================================================
    //              🔹 إرجاع الكتاب
    // ====================================================
    public void returnBook() {
        this.isBorrowed = false;
        this.dueDate = null;
        this.borrowedBy = null;
    }

    // ====================================================
    //              🔹 هل الكتاب متأخر؟
    // ====================================================
    public boolean isOverdue() {
        return isBorrowed && dueDate != null && dueDate.isBefore(LocalDate.now());
    }

    // ====================================================
    //              🔹 تمثيل الكتاب للنصوص (للطباعة)
    // ====================================================
    @Override
    public String toString() {
        return "\nBook {" +
                "\n  Title = '" + title + '\'' +
                ",\n  Author = '" + author + '\'' +
                ",\n  ISBN = '" + isbn + '\'' +
                ",\n  Borrowed = " + isBorrowed +
                ",\n  Due Date = " + dueDate +
                (borrowedBy != null ? ",\n  Borrowed By = " + borrowedBy.getName() : "") +
                "\n}";
    }

    // ====================================================
    //              🔹 تحويل الكتاب لسطر قابل للحفظ في الملف
    // ====================================================
    public String toFileString() {
        return title + ";" +
               author + ";" +
               isbn + ";" +
               isBorrowed + ";" +
               (dueDate != null ? dueDate.toString() : "null") + ";" +
               (borrowedBy != null ? borrowedBy.getName() : "null");
    }

    // ====================================================
    //              🔹 إنشاء كتاب من سطر في الملف
    // ====================================================
    public static Book fromFileString(String line) {
        String[] parts = line.split(";", -1); // -1 للحفاظ على جميع الفواصل
        Book book = new Book(parts[0], parts[1], parts[2]);
        book.setBorrowed(Boolean.parseBoolean(parts[3]));

        if (!parts[4].equals("null") && !parts[4].isBlank()) {
            book.setDueDate(LocalDate.parse(parts[4]));
        }

        if (!parts[5].equals("null") && !parts[5].isBlank()) {
            // إذا أردت لاحقاً ربط اسم المستخدم بكائن User حقيقي
            User u = new User(parts[5], "", "User");
            book.setBorrowedBy(u);
        }

        return book;
    }
}
