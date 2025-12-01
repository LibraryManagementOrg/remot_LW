package service;
import model.User;

public class EmailService implements NotificationObserver {
    @Override
    public void update(User user, String message) {
        // هنا نستخدم الإيميل الذي أدخله المستخدم
        System.out.println("--------------------------------------------------");
        System.out.println("📨 SENDING EMAIL...");
        System.out.println("To: " + user.getEmail()); // الإيميل من اليوزر
        System.out.println("Subject: Library Notification");
        System.out.println("Body: Dear " + user.getName() + ", " + message);
        System.out.println("--------------------------------------------------");
    }
}