

import model.Book;
import model.BorrowRecord;
import model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Enable Mockito
class ReminderServiceTest {

    @Mock
    private EmailService emailServiceMock; // Create a fake EmailService

    @InjectMocks
    private ReminderService reminderService; // Inject the fake service into ReminderService

    @Test
    void testSendOverdueReminders() {
        // 1. Setup Data (Arrange)
        User user1 = new User("U1", "Ali", "ali@test.com");
        Book book1 = new Book("B1", "Java 101", "Author A");
        Book book2 = new Book("B2", "Testing", "Author B");

        // Create a record that is overdue (Due date was yesterday)
        BorrowRecord record1 = new BorrowRecord(book1, user1);
        // We need to simulate that this book was borrowed a long time ago
        // Since we can't easily change private fields, assume we have a constructor or setter needed,
        // OR rely on the fact that ReminderService takes 'checkDate'.
        
        // Let's say the book was due on 2023-10-01
        // We create a "Spy" or just handle the logic via the checkDate parameter in the method.
        
        // *Trick*: BorrowRecord sets due date to +28 days from creation.
        // So if we pass a checkDate that is +30 days from now, it will be overdue.
        
        List<BorrowRecord> records = Arrays.asList(record1);
        
        // Fake "Today" is 30 days in the future relative to when the record was created
        LocalDate futureDate = LocalDate.now().plusDays(30); 

        // 2. Execute Logic (Act)
        reminderService.sendOverdueReminders(records, futureDate);

        // 3. Verify (Assert)
        // Check if emailService.sendEmail was called exactly once with specific arguments
        verify(emailServiceMock, times(1)).sendEmail("ali@test.com", "You have 5 overdue book(s).");
    }
}