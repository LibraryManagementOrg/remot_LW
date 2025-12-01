package model;

import java.time.LocalDate;

public class Book {

    private String title;
    private String author;
    private String isbn;
    private boolean isBorrowed;
    private LocalDate dueDate;
    private User borrowedBy;
    private boolean fineIssued;

    public Book(String title, String author, String isbn) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.isBorrowed = false;
        this.dueDate = null;
        this.borrowedBy = null;
        this.fineIssued = false;
    }

    // Getters & Setters
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getIsbn() { return isbn; }
    public boolean isBorrowed() { return isBorrowed; }
    public LocalDate getDueDate() { return dueDate; }
    public User getBorrowedBy() { return borrowedBy; }
    public boolean isFineIssued() { return fineIssued; }

    public void setBorrowed(boolean borrowed) { this.isBorrowed = borrowed; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public void setBorrowedBy(User user) { this.borrowedBy = user; }
    public void setFineIssued(boolean fineIssued) { this.fineIssued = fineIssued; }

    // Logic
    public void borrow(User user) {
        this.isBorrowed = true;
        this.dueDate = LocalDate.now().plusDays(28);
        this.borrowedBy = user;
    }

    public void returnBook() {
        this.isBorrowed = false;
        this.dueDate = null;
        this.borrowedBy = null;
    }

    public boolean isOverdue() {
        return isBorrowed && dueDate != null && dueDate.isBefore(LocalDate.now());
    }

    // ✅✅✅ هذه الدالة المسؤولة عن الكتابة داخل الملف (تأكدي أنها موجودة)
    public String toFileString() {
        String borrowerName = (borrowedBy != null) ? borrowedBy.getName() : "null";
        String dateStr = (dueDate != null) ? dueDate.toString() : "null";
        
        // الترتيب: Title;Author;ISBN;isBorrowed;DueDate;BorrowerName;FineIssued
        return title + ";" + author + ";" + isbn + ";" + isBorrowed + ";" + dateStr + ";" + borrowerName + ";" + fineIssued;
    }

    // ✅✅✅ هذه الدالة المسؤولة عن القراءة من الملف
    public static Book fromFileString(String line) {
        String[] parts = line.split(";", -1);
        if (parts.length < 3) return null;

        Book book = new Book(parts[0], parts[1], parts[2]);

        if (parts.length > 3) book.setBorrowed(Boolean.parseBoolean(parts[3]));
        
        if (parts.length > 4 && !parts[4].equals("null")) {
            book.setDueDate(LocalDate.parse(parts[4]));
        }

        if (parts.length > 5 && !parts[5].equals("null")) {
            // هنا ننشئ يوزر مؤقت يحمل الاسم فقط
            User u = new User(parts[5], "", "", "User");
            book.setBorrowedBy(u);
        }
        
        if (parts.length > 6) {
             book.setFineIssued(Boolean.parseBoolean(parts[6]));
        }

        return book;
    }

    @Override
    public String toString() {
        return title + " by " + author + " (ISBN: " + isbn + ")";
    }
}