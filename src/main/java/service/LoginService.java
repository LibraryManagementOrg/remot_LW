package service;

import model.User;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

public class LoginService {

    // --------- LOOP for login until correct credentials ---------
    public User loginLoop() {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("Enter Username: ");
            String username = scanner.nextLine();

            System.out.print("Enter Password: ");
            String password = scanner.nextLine();

            User user = login(username, password);

            if (user == null) {
                System.out.println("❌ Wrong username or password! Try again.\n");
            } else if ("ERROR".equalsIgnoreCase(user.getRole())) {
                System.out.println("⚠ " + user.getName()); // طباعة رسالة الخطأ
                return null;  // توقف لوجود مشكلة في الملف
            } else {
                System.out.println("✅ Login successful! Your role is: " + user.getRole());
                return user;  // إرجاع المستخدم بكامل بياناته
            }
        }
    }

    // --------- LOGIN with file ---------
    public User login(String username, String password) {
        String filePath = "src/main/resources/users.txt";

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;

            while ((line = br.readLine()) != null) {
                // تخطي الأسطر الفارغة
                if (line.trim().isEmpty()) continue;

                String[] parts = line.split(",");

                // التعديل هنا: نقبل السطر إذا كان فيه 3 خانات أو أكثر
                if (parts.length >= 3) {
                    String fileUser = parts[0].trim();
                    String filePass = parts[1].trim();
                    String role = parts[2].trim();

                    // التحقق من المطابقة
                    if (fileUser.equals(username) && filePass.equals(password)) {
                        
                        double fine = 0.0;
                        String email = "";

                        // قراءة الغرامة (الخانة 4) إن وجدت
                        if (parts.length >= 4) {
                            try {
                                fine = Double.parseDouble(parts[3].trim());
                            } catch (NumberFormatException e) {
                                fine = 0.0;
                            }
                        }

                        // قراءة الإيميل (الخانة 5) إن وجدت
                        if (parts.length >= 5) {
                            email = parts[4].trim();
                        }

                        // ✅ إرجاع يوزر كامل (مع الغرامة والإيميل)
                        return new User(fileUser, filePass, role, fine, email);
                    }
                }
            }
        } catch (IOException e) {
            // نرجع User يحمل رسالة الخطأ للتعامل معها في اللوب
            return new User("ERROR: Cannot read users file!", "", "ERROR");
        }

        return null; // لا يوجد مستخدم مطابق
    }
}