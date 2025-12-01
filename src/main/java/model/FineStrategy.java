package model;

/**
 * Strategy Interface for calculating fines.
 * This is part of the Strategy Design Pattern required in Sprint 5.
 * 
 * @author Student
 */
public interface FineStrategy {
    
    /**
     * Calculates the fine based on the number of overdue days.
     * 
     * @param overdueDays The number of days the item is overdue.
     * @return The calculated fine amount.
     */
    double calculateFine(long overdueDays);
}