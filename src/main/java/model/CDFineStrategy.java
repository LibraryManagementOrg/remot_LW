package model;

/**
 * Concrete Strategy for CDs.
 * Rule: 20 NIS per overdue day (Sprint 5.2).
 */
public class CDFineStrategy implements FineStrategy {
    
    @Override
    public double calculateFine(long overdueDays) {
        // غرامة السي دي 20 شيكل عن كل يوم تأخير
        return overdueDays * 20.0;
    }
}