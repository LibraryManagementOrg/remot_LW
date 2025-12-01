package model;

/**
 * استراتيجية حساب الغرامة للكتب.
 * القاعدة: 10 شيكل عن كل يوم تأخير.
 */
public class BookFineStrategy implements FineStrategy {
    @Override
    public double calculateFine(long overdueDays) {
        return overdueDays * 10.0;
    }
}