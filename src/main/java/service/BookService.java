package service;
import java.util.ArrayList;
import java.util.List;
import model.Book;


public class BookService {
	 private List<Book> books = new ArrayList<>();

	 public void addBook(String title, String author, String isbn, AdminService adminService) {
	        if (!adminService.isLoggedIn()) {
	            System.out.println("Access denied! Please log in as admin."); // ❌ غير مسموح
	            return;
	        }

	        books.add(new Book(title, author, isbn));
	        //System.out.println("Book added successfully: " + title); // ✅ نجحت العملية
	    }

	 
	    public List<Book> searchBook(String keyword) {
	        List<Book> results = new ArrayList<>();
	        for (Book book : books) {
	            if (book.getTitle().toLowerCase().contains(keyword.toLowerCase()) ||
	                book.getAuthor().toLowerCase().contains(keyword.toLowerCase()) ||
	                book.getIsbn().equalsIgnoreCase(keyword)) {
	                results.add(book);
	            }
	        }

	        if (results.isEmpty()) {
	            System.out.println("No books found matching: " + keyword);
	        } else {
	            System.out.println("Search results for '" + keyword + "':");
	            for (Book b : results) {
	                System.out.println(b);
	            }
	        }

	        return results;
	    }

	    public List<Book> getAllBooks() {
	        return books;
	    }

}
