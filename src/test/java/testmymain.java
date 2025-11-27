import model.*;
import service.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class testmymain {

    @Test
    public void testBorrowBookSuccess() {
        BorrowService borrowService = new BorrowService();
        User user = new User("Layal");
        Book book = new Book("Java Basics", "Oracle", "12345");

        BorrowRecord record = borrowService.borrowBook(user, book);
        assertTrue(book.isBorrowed());
        assertEquals(record.getDueDate(), record.getBorrowDate().plusDays(28));
    }

    @Test
    public void testOverdueBook() {
        BorrowService borrowService = new BorrowService();
        User user = new User("Layal");
        Book book = new Book("Algorithms", "Cormen", "9876");
        BorrowRecord record = borrowService.borrowBook(user, book);

        // محاكاة أن الكتاب تأخر 29 يوم
        record.getDueDate().minusDays(29); // هنا ستعدل لاحقًا بمحاكاة وقت فعلي
        assertFalse(record.isOverdue()); // مجرد مثال لاختبار منطق الكود
    }

    @Test
    public void testPayFine() {
        FineService fineService = new FineService();
        User user = new User("Layal");
        user.setOutstandingFine(10);
        fineService.payFine(user, 4);
        assertEquals(6, user.getOutstandingFine());
    }
}
