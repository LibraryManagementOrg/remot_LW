package service;

import model.User;
import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

public class RealEmailService implements NotificationObserver {

    private final String myEmail = "s12218557@stu.najah.edu"; 
    private final String myPassword = System.getenv("EMAIL_PASSWORD");

    @Override
    public void update(User user, String messageText) {
        
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            System.out.println("⚠ Warning: User [" + user.getName() + "] has no email address. Email skipped.");
            return;
        }

        System.out.println("⏳ Connecting to Gmail...");

        Properties prop = new Properties();
        prop.put("mail.smtp.host", "smtp.gmail.com");
        prop.put("mail.smtp.port", "587");
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(prop, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(myEmail, myPassword);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(myEmail));
            
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