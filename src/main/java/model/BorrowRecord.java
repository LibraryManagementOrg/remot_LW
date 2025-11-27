package model;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
public class BorrowRecord {
	
	 private Book book;
	    private User user;
	    private LocalDate borrowDate;
	    private LocalDate dueDate;
	    private boolean returned;

	    public BorrowRecord(Book book, User user) {
	        this.book = book;
	        this.user = user;
	        this.borrowDate = LocalDate.now();
	        this.dueDate = borrowDate.plusDays(28);
	        this.returned = false;
	    }

	    public Book getBook() {
	        return book;
	    }

	    public User getUser() {
	        return user;
	    }

	    public LocalDate getBorrowDate() {
	        return borrowDate;
	    }

	    public LocalDate getDueDate() {
	        return dueDate;
	    }

	    public boolean isReturned() {
	        return returned;
	    }

	    public void setReturned(boolean returned) {
	        this.returned = returned;
	    }

	    public boolean isOverdue() {
	        return !returned && LocalDate.now().isAfter(dueDate);
	    }

	    public long getDaysOverdue() {
	        if (!isOverdue()) return 0;
	        return ChronoUnit.DAYS.between(dueDate, LocalDate.now());
	    }

	    @Override
	    public String toString() {
	        return "BorrowRecord{" +
	                "book=" + book +
	                ", user=" + user +
	                ", borrowDate=" + borrowDate +
	                ", dueDate=" + dueDate +
	                ", returned=" + returned +
	                '}';
	    }

}
