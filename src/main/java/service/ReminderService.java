package service;

import model.Book;
import model.User;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit; // مكتبة لحساب الفرق بالأيام
import java.util.List;

public class ReminderService {
    
    private NotificationObserver observer;
    private UserService userService;

    public ReminderService(NotificationObserver observer, UserService userService) {
        this.observer = observer;
        this.userService = userService;
    }

    public void sendOverdueReminders(List<Book> allBooks) {
        System.out.println("Checking books...");
        LocalDate today = LocalDate.now();

        for (Book book : allBooks) {
            if (book.isBorrowed() && book.getBorrowedBy() != null) {
                
                // جلب اليوزر الكامل عشان الإيميل
                User fullUser = userService.findUserByName(book.getBorrowedBy().getName());
                if (fullUser == null) continue;

                // 1. حالة التأخير (المطلوبة في الواجب)
                if (book.isOverdue()) {
                    String msg = "🚨 URGENT: The book '" + book.getTitle() + "' is OVERDUE! Please return it.";
                    observer.update(fullUser, msg);
                } 
                
                // 2. حالة التنبيه المبكر (إضافة من عندك)
                else if (book.getDueDate() != null) {
                    // حساب الفرق بالأيام بين اليوم وموعد الإرجاع
                    long daysLeft = ChronoUnit.DAYS.between(today, book.getDueDate());
                    
                    // إذا ضايل 3 أيام أو أقل
                    if (daysLeft > 0 && daysLeft <= 3) {
                        String msg = "⏳ REMINDER: You have " + daysLeft + " days left to return '" + book.getTitle() + "'.";
                        observer.update(fullUser, msg);
                    }
                }
            }
        }
    }
}