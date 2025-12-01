package service;

import model.User;
import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

public class RealEmailService implements NotificationObserver {

    // 🔴 ضعي إيميل المرسل هنا (إيميلك أنت كأدمن)
    private final String myEmail = "s12217555@stu.najah.edu"; 
    
    // 🔴 ضعي كلمة مرور التطبيق هنا (وليس كلمة السر العادية)
    // الشرح في الخطوة 3 بالأسفل كيف تجيبيها
    private final String myPassword = "njtj bqsx mabm ktgx"; 

    @Override
    public void update(User user, String messageText) {
        System.out.println("⏳ Connecting to Gmail...");

        Properties prop = new Properties();
        prop.put("mail.smtp.host", "smtp.gmail.com");
        prop.put("mail.smtp.port", "587");
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.starttls.enable", "true"); // TLS

        Session session = Session.getInstance(prop, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(myEmail, myPassword);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(myEmail));
            
            // هنا نرسل للإيميل المسجل في حساب المستخدم
            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(user.getEmail()) 
            );
            message.setSubject("Library Overdue Alert 📚");
            message.setText("Hello " + user.getName() + ",\n\n" + messageText + "\n\nPlease return it ASAP.\n\nAdmin.");

            Transport.send(message);

            System.out.println("✅ REAL Email Sent Successfully to: " + user.getEmail());

        } catch (MessagingException e) {
            e.printStackTrace();
            System.out.println("❌ Failed to send email. Check internet or password.");
        }
    }
}