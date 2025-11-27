package model;

import java.time.LocalDate;

public class Book {
    private String title;
    private String author;
    private String isbn;
    private boolean borrowed;
    private LocalDate dueDate;

    public Book(String title, String author, String isbn) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.borrowed = false;
        this.dueDate = null;
    }

    // Getters
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getIsbn() { return isbn; }
    public boolean isBorrowed() { return borrowed; }
    public LocalDate getDueDate() { return dueDate; }

    // Setters
    public void setBorrowed(boolean borrowed) { this.borrowed = borrowed; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    // Borrow book for 28 days
    public void borrow() {
        if (borrowed) {
            throw new IllegalStateException("Book is already borrowed!");
        }
        borrowed = true;
        dueDate = LocalDate.now().plusDays(28);
    }

    // Return book
    public void returnBook() {
        borrowed = false;
        dueDate = null;
    }

    // Check overdue
    public boolean isOverdue() {
        return borrowed && dueDate != null && dueDate.isBefore(LocalDate.now());
    }

    @Override
    public String toString() {
        return "Book{" +
                "title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", isbn='" + isbn + '\'' +
                ", borrowed=" + borrowed +
                ", dueDate=" + dueDate +
                '}';
    }
}
