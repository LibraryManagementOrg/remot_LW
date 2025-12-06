package service;

import model.User;
import model.media;
import java.io.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class UserService {

    private List<User> users;
    private User loggedInUser;
    
    private String filePath = "src/main/resources/users.txt";

    public UserService() {
        users = new ArrayList<>();
        loadUsersFromFile();
    }

    public UserService(String testFilePath) {
        this.filePath = testFilePath;
        users = new ArrayList<>();
        loadUsersFromFile();
    }

    private void loadUsersFromFile() {
        try (BufferedReader br = new BufferedReader(new FileReader(this.filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                
                String[] parts = line.split(",");
                
                if (parts.length >= 3) {
                    String username = parts[0].trim();
                    String password = parts[1].trim();
                    String role = parts[2].trim();
                    
                    double fine = 0.0;
                    if (parts.length >= 4) {
                        try { 
                            fine = Double.parseDouble(parts[3].trim()); 
                        } catch (NumberFormatException e) { 
                            fine = 0.0; 
                        }
                    }

                    String email = "";
                    if (parts.length >= 5) {
                        email = parts[4].trim();
                    }

                    User user = new User(username, password, role, fine, email);
                    users.add(user);
                }
            }
        } catch (IOException e) {
            System.out.println("⚠ Error reading users file: " + e.getMessage());
        }
    }

    public void saveUsersToFile() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(this.filePath))) {
            for (User u : users) {
                pw.println(u.getName() + "," + 
                           u.getPassword() + "," + 
                           u.getRole() + "," + 
                           u.getOutstandingFine() + "," + 
                           u.getEmail());
            }
        } catch (IOException e) {
            System.out.println("❌ Error saving users file: " + e.getMessage());
        }
    }

    // 🔴 تم إزالة رسائل النجاح والفشل إلى mymain
    public User login(String username, String password, BookService bookService) {
        for (User user : users) {
            if (user.getName().equals(username) && user.getPassword().equals(password)) {
                loggedInUser = user;
                System.out.println("✅ " + username + " logged in successfully as " + user.getRole() + ".");

                if (bookService != null) {
                    checkAndApplyFinesForAllUsers(bookService);
                }
                return user;
            }
        }
        System.out.println("❌ Invalid username or password.");
        return null;
    }

    // 🔴 تم التعديل: يعيد String
    public String logout() {
        if (loggedInUser != null) {
            String msg = "🔒 " + loggedInUser.getName() + " logged out successfully.";
            loggedInUser = null;
            return msg;
        }
        return "You were not logged in.";
    }

    public boolean isLoggedIn() { return loggedInUser != null; }
    public User getLoggedInUser() { return loggedInUser; }

    // 🔴 تم التعديل: يعيد String (بدلاً من void)
    public String payFine(User user, double amount, BookService bookService) {
        if (loggedInUser == null || !loggedInUser.equals(user)) {
            return "❌ Access denied! User must be logged in to pay fine.";
        }

        if (amount <= 0) {
            return "❌ Invalid amount. Please enter a positive value.";
        }

        if (amount > user.getOutstandingFine()) {
            return String.format("❌ Error: You entered %.2f, but your fine is only %.2f", amount, user.getOutstandingFine());
        }

        user.setOutstandingFine(user.getOutstandingFine() - amount);

        if (user.getOutstandingFine() == 0 && bookService != null) {
            boolean itemsReturned = false;
            for (media m : bookService.getAllBooks()) {
                if (m.isBorrowed() &&
                    m.getBorrowedBy() != null &&
                    m.getBorrowedBy().getName().equalsIgnoreCase(user.getName()) &&
                    m.isOverdue()) {

                    // لا يوجد طباعة هنا
                    m.setBorrowed(false);
                    m.setBorrowedBy(null);
                    m.setDueDate(null);
                    m.setFineIssued(false);

                    itemsReturned = true;
                }
            }
            if (itemsReturned) bookService.saveBooksToFile();
        }

        saveUsersToFile(); 
        return String.format("✅ Payment successful. Remaining balance: %.2f", user.getOutstandingFine());
    }

  public void checkAndApplyFinesForAllUsers(BookService bookService) {
        boolean usersUpdated = false;
        boolean booksUpdated = false;

        for (media m : bookService.getAllBooks()) {
            if (m.isBorrowed() && m.isOverdue() && !m.isFineIssued() && m.getBorrowedBy() != null) {
                User borrower = findUserByName(m.getBorrowedBy().getName());
                if (borrower != null) {
                    double fine = m.getFineAmount();
                    borrower.setOutstandingFine(borrower.getOutstandingFine() + fine);
                    m.setFineIssued(true);
                    usersUpdated = true;
                    booksUpdated = true;
                }
            }
        }
        if (usersUpdated) saveUsersToFile();
        if (booksUpdated) bookService.saveBooksToFile();
    }

    // 🔴 تم التعديل: يعيد String (بدلاً من boolean)
    public String deleteUser(String username) {
        User userToRemove = null;
        for (User u : users) {
            if (u.getName().equalsIgnoreCase(username)) {
                userToRemove = u;
                break;
            }
        }
        if (userToRemove != null) {
            users.remove(userToRemove);
            saveUsersToFile();
            return "🗑 User [" + username + "] deleted.";
        }
        return "❌ User [" + username + "] not found.";
    }

    public void addFine(User user, double amount) {
        if (amount <= 0) return;
        user.setOutstandingFine(user.getOutstandingFine() + amount);
        saveUsersToFile();
    }

    public User findUserByName(String username) {
        for (User u : users) {
            if (u.getName().equalsIgnoreCase(username)) return u;
        }
        return null;
    }

    public List<User> getAllUsers() { return users; }
}