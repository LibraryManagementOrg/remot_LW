package service;

import model.BorrowRecord;
import model.User;

public class FineService {
    private static final double DAILY_FINE = 1.0; // وحدة غرامة لكل يوم تأخير
    private UserService userService;

    public FineService(UserService userService) {
        this.userService = userService;
    }

    // حساب الغرامة
    public double calculateFine(BorrowRecord record) {
        return record.getDaysOverdue() * DAILY_FINE;
    }

    // تطبيق الغرامة على مستخدم
    public void applyFine(User user, BorrowRecord record) {
        if (!userService.isLoggedIn() || !userService.getLoggedInUser().equals(user)) {
            System.out.println("❌ Access denied! User must be logged in to apply fine.");
            return;
        }
        double fine = calculateFine(record);
        user.setOutstandingFine(user.getOutstandingFine() + fine);
        System.out.println("⚠ Fine applied: " + fine + " for overdue book: " + record.getBook().getTitle());
    }

    // دفع الغرامة
    public void payFine(User user, double amount) {
        if (!userService.isLoggedIn() || !userService.getLoggedInUser().equals(user)) {
            System.out.println("❌ Access denied! User must be logged in to pay fine.");
            return;
        }

        double remaining = user.getOutstandingFine() - amount;
        user.setOutstandingFine(Math.max(0, remaining));
        System.out.println("✅ Fine paid successfully. Remaining fine: " + user.getOutstandingFine());
    }
}
