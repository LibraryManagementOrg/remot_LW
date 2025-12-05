package service;

import model.User;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

public class LoginService {

    // ✅ متغير للمسار (بدلاً من كتابته داخل الدالة)
    private String filePath = "src/main/resources/users.txt";

    // 1️⃣ الكونستركتور الافتراضي (يستخدمه البرنامج الرئيسي)
    public LoginService() {
        // يبقى على المسار الافتراضي
    }

    // 2️⃣ كونستركتور مخصص للاختبار (يقبل مساراً مختلفاً)
    public LoginService(String testFilePath) {
        this.filePath = testFilePath;
    }

    // --------- LOOP for login ---------
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
                System.out.println("⚠ " + user.getName());
                return null;
            } else {
                System.out.println("✅ Login successful! Your role is: " + user.getRole());
                return user;
            }
        }
    }

    // --------- LOGIN with file ---------
    public User login(String username, String password) {
        // ✅ نستخدم المتغير this.filePath
        try (BufferedReader br = new BufferedReader(new FileReader(this.filePath))) {
            String line;

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] parts = line.split(",");

                if (parts.length >= 3) {
                    String fileUser = parts[0].trim();
                    String filePass = parts[1].trim();
                    String role = parts[2].trim();

                    if (fileUser.equals(username) && filePass.equals(password)) {
                        
                        double fine = 0.0;
                        String email = "";

                        if (parts.length >= 4) {
                            try { fine = Double.parseDouble(parts[3].trim()); } catch (Exception e) { fine = 0.0; }
                        }

                        if (parts.length >= 5) {
                            email = parts[4].trim();
                        }

                        return new User(fileUser, filePass, role, fine, email);
                    }
                }
            }
        } catch (IOException e) {
            return new User("ERROR: Cannot read users file!", "", "ERROR");
        }

        return null;
    }
}