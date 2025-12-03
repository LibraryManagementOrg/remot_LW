package service;

import model.User;
import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

public class RealEmailService implements NotificationObserver {

    // 🔴 1. هنا ضعي إيميل الـ GMAIL الذي استخرجتِ الباسورد من إعداداته
    // ❌ لا تضعي إيميل الجامعة هنا
    private final String myEmail = "s12218557@stu.najah.edu"; 
    
    // 🔴 2. هنا ضعي الـ 16 حرف التي ظهرت لك في الخطوة 4
    private final String myPassword = "ylvc iqnl bnsh klxy"; 

    @Override
    public void update(User user, String messageText) {
        System.out.println("⏳ Connecting to Gmail...");

        Properties prop = new Properties();
        // إعدادات سيرفر Gmail
        prop.put("mail.smtp.host", "smtp.gmail.com");
        prop.put("mail.smtp.port", "587");
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(prop, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                // الكود يستخدم إيميلك وباسورد التطبيق للدخول للسيرفر
                return new PasswordAuthentication(myEmail, myPassword);
            }
        });

        try {
            Message message = new MimeMessage(session);
            // المرسل هو إيميلك الجيميل
            message.setFrom(new InternetAddress(myEmail));
            
            // المستقبل هو الطالب (يأخذ الإيميل من ملف users.txt)
            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(user.getEmail()) 
            );
            
            message.setSubject("Library Overdue Alert 📚");
            message.setText("Hello " + user.getName() + ",\n\n" + messageText + "\n\nPlease return it ASAP.\n\nAdmin.");

            Transport.send(message);

            System.out.println("✅ REAL Email Sent Successfully to: " + user.getEmail());

        } catch (MessagingException e) {
            System.out.println("❌ Failed to send email via Gmail.");
            e.printStackTrace();
        }
    }
}