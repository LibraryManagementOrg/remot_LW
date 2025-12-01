package service;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import model.User;

public class UserService {
    private List<User> users;
    private User loggedInUser;
    private final String FILE_PATH = "src/main/resources/users.txt";

    public UserService() {
        users = new ArrayList<>();
        loadUsersFromFile();
    }

    private void loadUsersFromFile() {
        users.clear(); // تنظيف القائمة قبل التحميل
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = br.readLine()) != null) {
                User u = User.fromFileString(line);
                if (u != null) {
                    users.add(u);
                }
            }
        } catch (IOException e) {
            // الملف قد يكون غير موجود عند أول تشغيل
        }
    }

    // حفظ مستخدم جديد في الملف والقائمة
    public void addUser(User newUser) {
        users.add(newUser);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
            bw.write(newUser.toFileString());
            bw.newLine();
        } catch (IOException e) {
            System.out.println("❌ Error saving user to file!");
        }
    }

    public User login(String username, String password) {
        // تحديث القائمة من الملف قبل اللوجين لضمان وجود المستخدمين الجدد
        loadUsersFromFile(); 
        
        for (User user : users) {
            if (user.getName().equals(username) && user.getPassword().equals(password)) {
                loggedInUser = user;
                System.out.println("✅ Logged in as: " + user.getName());
                return user;
            }
        }
        return null;
    }

    public void logout() {
        loggedInUser = null;
        System.out.println("🔒 Logged out.");
    }

    public boolean isLoggedIn() {
        return loggedInUser != null;
    }

    public User getLoggedInUser() {
        return loggedInUser;
    }

    public User findUserByName(String name) {
        loadUsersFromFile(); // تحديث
        for (User u : users) {
            if (u.getName().equalsIgnoreCase(name)) return u;
        }
        return null;
    }

    // دفع الغرامة وتحديث الملف
    public void payFine(User user, double amount) {
        if (amount <= 0) return;
        
        double newFine = Math.max(0, user.getOutstandingFine() - amount);
        user.setOutstandingFine(newFine);
        updateUserFile(); // تحديث الملف لحفظ الغرامة الجديدة
        System.out.println("✅ Payment successful. Remaining fine: " + newFine);
    }
    
    // إعادة كتابة الملف بالكامل (لتحديث الغرامات مثلاً)
    private void updateUserFile() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_PATH))) {
            for (User u : users) {
                bw.write(u.toFileString());
                bw.newLine();
            }
        } catch (IOException e) {
            System.out.println("❌ Error updating users file!");
        }
    }
}