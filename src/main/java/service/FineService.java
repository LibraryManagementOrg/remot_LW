package service;

import model.BorrowRecord;
import model.User;
//tgrt
public class FineService {
    private static final double DAILY_FINE = 1.0; // وحدة غرامة لكل يوم تأخير

    public double calculateFine(BorrowRecord record) {
        return record.getDaysOverdue() * DAILY_FINE;
    }

    public void applyFine(User user, BorrowRecord record) {
        double fine = calculateFine(record);
        user.setOutstandingFine(user.getOutstandingFine() + fine);
    }

    public void payFine(User user, double amount) {
        double remaining = user.getOutstandingFine() - amount;
        user.setOutstandingFine(Math.max(0, remaining));
    }
}
