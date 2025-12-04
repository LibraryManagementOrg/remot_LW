package service;

import model.User;
import model.media; 
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class ReminderService {
    
    private NotificationObserver observer;
    private UserService userService;

    public ReminderService(NotificationObserver observer, UserService userService) {
        this.observer = observer;
        this.userService = userService;
    }

    public void sendOverdueReminders(List<media> allMedia) {
        System.out.println("📧 Calculating overdue items per user...");
        LocalDate today = LocalDate.now();
        boolean sentAny = false;

        // 1. ندور على كل المستخدمين المسجلين في النظام
        for (User user : userService.getAllUsers()) {
            
            // 🛑 شرط: نبعت فقط لليوزرز (مش للأدمن ولا الموظف)
            if (!"User".equalsIgnoreCase(user.getRole())) {
                continue; 
            }

            // التحقق من وجود إيميل
            if (user.getEmail() == null || user.getEmail().isEmpty()) {
                continue;
            }

            // متغيرات لتجميع المعلومات لهذا اليوزر
            int overdueCount = 0;
            StringBuilder messageDetails = new StringBuilder();

            // 2. فحص جميع العناصر لمعرفة ما يملكه هذا اليوزر منها ومتأخر
            for (media item : allMedia) {
                if (item.isBorrowed() && 
                    item.getBorrowedBy() != null &&
                    item.getBorrowedBy().getName().equalsIgnoreCase(user.getName()) && // العنصر مع هذا اليوزر
                    item.isOverdue()) { // العنصر متأخر

                    overdueCount++;
                    long daysOverdue = ChronoUnit.DAYS.between(item.getDueDate(), today);
                    
                    // ✅ التعديل هنا: جلب النوع (Book أو CD)
                    String type = item.getClass().getSimpleName(); 
                    
                    // إضافته للرسالة بالشكل: - [Book] Title ...
                    messageDetails.append(String.format("- [%s] %s (Overdue by %d days)\n", type, item.getTitle(), daysOverdue));
                }
            }

            // 3. إذا كان لدى المستخدم عناصر متأخرة، نرسل الإيميل المجمع
            if (overdueCount > 0) {
                // النص المطلوب مع الساعة الرملية
                String header = "⏳ You have " + overdueCount + " overdue item(s).";
                
                String fullMessage = header + "\n\nDetails:\n" + messageDetails.toString();
                
                // إرسال الإيميل
                observer.update(user, fullMessage);
                sentAny = true;
            }
        }
        
        if (!sentAny) {
            System.out.println("✅ No overdue emails needed today.");
        }
    }
}