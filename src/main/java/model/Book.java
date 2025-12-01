package model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Book {

    private String title;
    private String author;
    private String isbn;

    private boolean isBorrowed;
    private LocalDate dueDate;
    private User borrowedBy;
    private boolean fineIssued;

    private static final double DAILY_FINE = 1.0;

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
        this.fineIssued = false;
    }

    // 🔹 إرجاع الكتاب
    public void returnBook() {
        this.isBorrowed = false;
        this.dueDate = null;
        this.borrowedBy = null;
        this.fineIssued = false;
    }

    // 🔹 هل الكتاب متأخر؟
    public boolean isOverdue() {
        return isBorrowed && dueDate != null && dueDate.isBefore(LocalDate.now());
    }

    // 🔹 حساب الغرامة
    public double getFineAmount() {
        if (!isOverdue()) return 0;
        long daysOverdue = ChronoUnit.DAYS.between(dueDate, LocalDate.now());
        return daysOverdue * DAILY_FINE;
    }

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

    // 🔹 تحويل إلى سطر للحفظ
    public String toFileString() {
        return title + ";" +
                author + ";" +
                isbn + ";" +
                isBorrowed + ";" +
                (dueDate != null ? dueDate : "null") + ";" +
                (borrowedBy != null ? borrowedBy.getName() : "null") + ";" +
                fineIssued;
    }

    // 🔹 استعادة كتاب من السطر
    public static Book fromFileString(String line) {
        if (line == null || line.isBlank()) return null;

        String[] parts = line.split(";", -1);

        if (parts.length < 3) return null;

        Book book = new Book(parts[0], parts[1], parts[2]);

        if (parts.length > 3)
            book.setBorrowed(Boolean.parseBoolean(parts[3]));

        if (parts.length > 4 && !parts[4].equals("null"))
            book.setDueDate(LocalDate.parse(parts[4]));

        if (parts.length > 5 && !parts[5].equals("null"))
            book.setBorrowedBy(new User(parts[5], "", "User"));

        if (parts.length > 6)
            book.setFineIssued(Boolean.parseBoolean(parts[6]));

        return book;
    }
}
