import service.*;
import model.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class mymain {
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		/////////////////Sprint 1//////////////////////////////
		//1
		/*
		  AdminService adminService = new AdminService();
	        // تجربة دخول صحيحة
	        String msg1 = adminService.login("Layal", "1234");
	        System.out.println(msg1);
	        // تجربة دخول خاطئة
	        String msg2 = adminService.login("wrongUser", "wrongPass");
	        System.out.println(msg2);*/
		//2
		/*
		AdminService adminService = new AdminService();
        BookService bookService = new BookService();

        // تسجيل الدخول
        System.out.println(adminService.login("Layal", "1234"));

        // إضافة كتاب أثناء تسجيل الدخول
        bookService.addBook("The Hobbit", "Tolkien", "1111", adminService);

        // تسجيل الخروج
        adminService.logout();

        // محاولة إضافة كتاب بعد الخروج
        bookService.addBook("1984", "Orwell", "2222", adminService);
        */
		
		//3+4
		/*
		 AdminService adminService = new AdminService();
        BookService bookService = new BookService();

        // محاولة إضافة كتاب بدون تسجيل دخول
        bookService.addBook("Test Book", "Someone", "0000", adminService);

        // تسجيل الدخول
        adminService.login("Layal", "1234");

        // ✅ US1.3 - إضافة الكتب
        bookService.addBook("The Hobbit", "J.R.R. Tolkien", "1111", adminService);
        bookService.addBook("1984", "George Orwell", "2222", adminService);
        bookService.addBook("Clean Code", "Robert C. Martin", "3333", adminService);

        // ✅ US1.4 - البحث عن الكتب
        bookService.searchBook("hobbit");     // بالعنوان
        bookService.searchBook("Orwell");     // بالمؤلف
        bookService.searchBook("3333");       // بالـ ISBN
        bookService.searchBook("Harry");      // غير موجود
		*/
        /////////////////////Sprint 2//////////////////////////////////////////
		// Initialize services
        BorrowService borrowService = new BorrowService();
        FineService fineService = new FineService();

        // Create a user and a book
        User user = new User("Layal");
        Book book = new Book("Java Programming", "Oracle", "12345");

        System.out.println("📘 Step 1: Borrow a book");
        BorrowRecord record = borrowService.borrowBook(user, book);
        System.out.println("Borrow successful:");
        System.out.println("Title: " + record.getBook().getTitle());
        System.out.println("Borrow date: " + record.getBorrowDate());
        System.out.println("Due date: " + record.getDueDate());
        System.out.println("--------------------------------------");

        // Simulate that 30 days have passed (for testing purposes)
        LocalDate fakeToday = record.getDueDate().plusDays(2);
        long daysOverdue = ChronoUnit.DAYS.between(record.getDueDate(), fakeToday);

        System.out.println("📅 Step 2: Check for overdue book");
        System.out.println("Book is overdue by " + daysOverdue + " days (simulated)");

        // Manually calculate a fine (as if the system detected it)
        double fine = daysOverdue * 1.0; // 1 unit per day overdue
        user.setOutstandingFine(fine);
        System.out.println("💰 Outstanding fine: " + user.getOutstandingFine());
        System.out.println("--------------------------------------");

        // Try to borrow another book before paying the fine
        Book book2 = new Book("Data Structures", "Cormen", "67890");
        System.out.println("📚 Trying to borrow another book before paying the fine...");
        try {
            borrowService.borrowBook(user, book2);
        } catch (IllegalStateException e) {
            System.out.println("❌ Failed: " + e.getMessage());
        }
        System.out.println("--------------------------------------");

        // Pay the fine
        System.out.println("💵 Step 3: Paying the fine in full...");
        fineService.payFine(user, user.getOutstandingFine());
        System.out.println("✅ Remaining fine after payment: " + user.getOutstandingFine());

        // Try borrowing again after payment
        System.out.println("📚 Trying again after payment...");
        BorrowRecord record2 = borrowService.borrowBook(user, book2);
        System.out.println("✅ Successfully borrowed the second book!");
        System.out.println("New due date: " + record2.getDueDate());
        System.out.println("--------------------------------------");

        // Show all borrowing records
        List<BorrowRecord> all = borrowService.getAllRecords();
        System.out.println("📋 All borrowing records:");
        for (BorrowRecord r : all) {
            System.out.println(r);
        }
		

	}

}
