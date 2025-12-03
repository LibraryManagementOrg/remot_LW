package model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public abstract class media {
    
    protected String title;
    protected String creator; 
    protected String id;      
    protected boolean isBorrowed;
    protected User borrowedBy;
    protected LocalDate dueDate;
    protected boolean fineIssued;
    
    // استراتيجية الغرامة (Design Pattern)
    protected FineStrategy fineStrategy;

    public media(String title, String creator, String id) {
        this.title = title;
        this.creator = creator;
        this.id = id;
        this.isBorrowed = false;
        this.fineIssued = false;
    }

    // ✅✅✅ هذه هي الدوال التي كانت تسبب المشكلة، يجب أن تكون موجودة هنا ✅✅✅
    public abstract int getLoanPeriod(); 
    public abstract double getDailyFine(); 
    // -------------------------------------------------------------------

    public double getFineAmount() {
        if (!isOverdue()) return 0.0;
        
        long daysOverdue = ChronoUnit.DAYS.between(dueDate, LocalDate.now());
        
        // الأولوية للاستراتيجية، وإذا لم توجد نستخدم getDailyFine
        if (fineStrategy != null) {
            return fineStrategy.calculateFine(daysOverdue);
        }
        return daysOverdue * getDailyFine();
    }

    public boolean isOverdue() {
        return isBorrowed && dueDate != null && LocalDate.now().isAfter(dueDate);
    }

    // Getters & Setters
    public String getTitle() { return title; }
    public String getCreator() { return creator; }
    public String getId() { return id; }
    
    public boolean isBorrowed() { return isBorrowed; }
    public void setBorrowed(boolean borrowed) { isBorrowed = borrowed; }
    
    public User getBorrowedBy() { return borrowedBy; }
    public void setBorrowedBy(User borrowedBy) { this.borrowedBy = borrowedBy; }
    
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    
    public boolean isFineIssued() { return fineIssued; }
    public void setFineIssued(boolean fineIssued) { this.fineIssued = fineIssued; }
    
    public void setFineStrategy(FineStrategy fineStrategy) {
        this.fineStrategy = fineStrategy;
    }

    @Override
    public String toString() {
        return "Type: " + this.getClass().getSimpleName() + " | " + title + " by " + creator + " | ID: " + id;
    }
}