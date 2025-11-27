package service;
import model.Admin;

public class AdminService {
	private Admin admin;
    private boolean loggedIn = false;

    public AdminService() {
        // حساب الأدمن الافتراضي
        admin = new Admin("Layal", "1234");
    }
    
 // دالة تسجيل الدخول
    public String login(String username, String password) {
        if (admin.getUsername().equals(username) && admin.getPassword().equals(password)) {
            loggedIn = true;
            return "Login successful!";   // ✅ حالة النجاح
        } else {
            loggedIn = false;
            return "Error: Invalid username or password.";  // ❌ حالة الخطأ
        }
    }
    
    public void logout() {
        loggedIn = false;
        System.out.println("Admin logged out successfully."); // ✅ رسالة تأكيد الخروج
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

}
