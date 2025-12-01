package service;

import java.util.ArrayList;
import java.util.List;

import model.Book;
import model.BorrowRecord;
import java.util.ArrayList;
import java.util.List;
import model.Book;
import model.User;

// هذا الكلاس لم نعد نستخدمه بشكل رئيسي في المين الجديد
// لأن العمليات انتقلت لـ BookService
public class BorrowService {
    // يمكنك تركه كما هو أو حذفه إذا لم يكن مستخدماً
}

/*public class BorrowService {
    private List<BorrowRecord> borrowRecords = new ArrayList<>();
    private UserService userService;

    // نمرّر UserService للتحقق من تسجيل الدخول
    public BorrowService(UserService userService) {
        this.userService = userService;
    }

    public BorrowRecord borrowBook(User user, Book book) {
        if (userService.getLoggedInUser() == null || !userService.getLoggedInUser().equals(user)) {
            System.out.println("❌ Access denied! User must be logged in to borrow books.");
            return null;
        }

        if (book.isBorrowed()) {
            System.out.println("❌ Book is already borrowed!");
            return null;
        }

        if (!user.canBorrow()) {
            System.out.println("❌ User has unpaid fines!");
            return null;
        }

        book.setBorrowed(true);
        BorrowRecord record = new BorrowRecord(book, user);
        borrowRecords.add(record);
        System.out.println("✅ Book borrowed successfully! Due date: " + record.getDueDate());
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
*/