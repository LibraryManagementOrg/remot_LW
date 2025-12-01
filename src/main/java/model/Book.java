package model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Book {

    private String title;
    private String author;
    private String isbn;

    private boolean isBorrowed;
    private LocalDate dueDate;
    private User borrowedBy; // المستخدم الذي استعار الكتاب
    private boolean fineIssued; // لتجنب مضاعفة الغرامة

    private static final double DAILY_FINE = 1.0; // قيمة الغرامة اليومية لكل كتاب متأخر

    public Book(String title, String author, String isbn) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.isBorrowed = false;
        this.dueDate = null;
        this.borrowedBy = null;
        this.fineIssued = false;
    }

    // ===== Getters =====
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getIsbn() { return isbn; }
    public boolean isBorrowed() { return isBorrowed; }
    public LocalDate getDueDate() { return dueDate; }
    public User getBorrowedBy() { return borrowedBy; }
    public boolean isFineIssued() { return fineIssued; }

    // ===== Setters =====
    public void setBorrowed(boolean borrowed) { this.isBorrowed = borrowed; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public void setBorrowedBy(User user) { this.borrowedBy = user; }
    public void setFineIssued(boolean fineIssued) { this.fineIssued = fineIssued; }

    // 🔹 استعارة الكتاب
    public void borrow(User user) {
        if (isBorrowed) {
            throw new IllegalStateException("Book is already borrowed!");
        }
        this.isBorrowed = true;
        this.dueDate = LocalDate.now().plusDays(28);
        this.borrowedBy = user;
        this.fineIssued = false; // عند استعارة جديدة، الغرامة لم تصدر بعد
    }

    // 🔹 إرجاع الكتاب
    public void returnBook() {
        this.isBorrowed = false;
        this.dueDate = null;
        this.borrowedBy = null;
        this.fineIssued = false; // عند الإرجاع، يتم تصفير العلم
    }

    // 🔹 هل الكتاب متأخر؟
    public boolean isOverdue() {
        return isBorrowed && dueDate != null && dueDate.isBefore(LocalDate.now());
    }

    // 🔹 حساب قيمة الغرامة للكتاب
    public double getFineAmount() {
        if (!isOverdue()) return 0;
        long daysOverdue = ChronoUnit.DAYS.between(dueDate, LocalDate.now());
        return daysOverdue * DAILY_FINE;
    }

    // 🔹 تمثيل الكتاب للنصوص (للطباعة)
    @Override
    public String toString() {
        return "\nBook {" +
                "\n  Title = '" + title + '\'' +
                ",\n  Author = '" + author + '\'' +
                ",\n  ISBN = '" + isbn + '\'' +
                ",\n  Borrowed = " + isBorrowed +
                ",\n  Due Date = " + dueDate +
                (borrowedBy != null ? ",\n  Borrowed By = " + borrowedBy.getName() : "") +
                ",\n  Fine Issued = " + fineIssued +
                "\n}";
    }

    // 🔹 تحويل الكتاب لسطر قابل للحفظ في الملف
    public String toFileString() {
        return title + ";" +
               author + ";" +
               isbn + ";" +
               isBorrowed + ";" +
               (dueDate != null ? dueDate.toString() : "null") + ";" +
               (borrowedBy != null ? borrowedBy.getName() : "null") + ";" +
               fineIssued;
    }

    // 🔹 إنشاء كتاب من سطر في الملف
    public static Book fromFileString(String line) {
        String[] parts = line.split(";", -1);
        Book book = new Book(parts[0], parts[1], parts[2]);

        boolean borrowed = Boolean.parseBoolean(parts[3]);
        book.setBorrowed(borrowed);

        if (!parts[4].equals("null") && !parts[4].isBlank()) {
            book.setDueDate(LocalDate.parse(parts[4]));
        }

        if (!parts[5].equals("null") && !parts[5].isBlank()) {
            User u = new User(parts[5], "", "User");
            book.setBorrowedBy(u);
        }

        if (parts.length > 6) {
            boolean fineIssued = Boolean.parseBoolean(parts[6]);
            book.setFineIssued(fineIssued);
        }

        return book;
    }
}
