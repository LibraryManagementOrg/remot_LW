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

    // 🔴 تم التعديل: أصبح يعيد int (عدد التذكيرات المرسلة)
    public int sendOverdueReminders(List<media> allMedia) {
        // العداد الذي سيتم إرجاعه
        int remindersSentCount = 0; 
        
        LocalDate today = LocalDate.now();

        // 1. ندور على كل المستخدمين المسجلين في النظام
        for (User user : userService.getAllUsers()) {
            
            // شرط: نبعت فقط لليوزرز (مش للأدمن ولا الموظف)
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
                    
                    String type = item.getClass().getSimpleName(); 
                    
                    // إضافته للرسالة بالشكل: - [Book] Title ...
                    messageDetails.append(String.format("- [%s] %s (Overdue by %d days)\n", type, item.getTitle(), daysOverdue));
                }
            }

            // 3. إذا كان لدى المستخدم عناصر متأخرة، نرسل الإيميل المجمع
            if (overdueCount > 0) {
                String header = "⏳ You have " + overdueCount + " overdue item(s).";
                
                String fullMessage = header + "\n\nDetails:\n" + messageDetails.toString();
                
                // إرسال الإيميل
                observer.update(user, fullMessage);
                remindersSentCount++; // 🛑 زيادة العداد
            }
        }
        
        // تم إزالة الطباعة التي كانت هنا (✅ No overdue emails needed today)
        // لأن الطباعة يجب أن تكون في mymain.java
        
        return remindersSentCount; // 🛑 إرجاع العداد
    }
}