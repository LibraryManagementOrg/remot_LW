package service;

import model.Book;
import model.User;
import model.BorrowRecord;

import java.util.ArrayList;
import java.util.List;

public class BorrowService {
    private List<BorrowRecord> borrowRecords = new ArrayList<>();

    public BorrowRecord borrowBook(User user, Book book) {
        if (book.isBorrowed()) {
            throw new IllegalStateException("❌ Book is already borrowed!");
        }

        if (!user.canBorrow()) {
            throw new IllegalStateException("❌ User has unpaid fines!");
        }

        book.setBorrowed(true);
        BorrowRecord record = new BorrowRecord(book, user);
        borrowRecords.add(record);
        return record;
    }

    public List<BorrowRecord> getOverdueBooks() {
        List<BorrowRecord> overdueList = new ArrayList<>();
        for (BorrowRecord r : borrowRecords) {
            if (r.isOverdue()) overdueList.add(r);
        }
        return overdueList;
    }

    public List<BorrowRecord> getAllRecords() {
        return borrowRecords;
    }
}
