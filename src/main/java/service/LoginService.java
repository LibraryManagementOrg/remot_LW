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
                System.out.println("⚠ " + user.getName()); // اسم المستخدم هنا يحمل رسالة الخطأ
                return null;  // توقف عند مشاكل بالملف
            } else {
                System.out.println("✅ Login successful! Your role is: " + user.getRole());
                return user;  // نرجع كائن المستخدم بالكامل
            }
        }
    }

    // --------- LOGIN with file ---------
    public User login(String username, String password) {
        String filePath = "src/main/resources/users.txt";

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;

            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");

                if (parts.length == 3) {
                    String fileUser = parts[0].trim();
                    String filePass = parts[1].trim();
                    String role = parts[2].trim();

                    if (fileUser.equals(username) && filePass.equals(password)) {
                        return new User(fileUser, filePass, role);
                    }
                }
            }
        } catch (IOException e) {
            // نرجع User يحمل رسالة الخطأ
            return new User("ERROR: Cannot read users file!", "", "ERROR");
        }

        return null; // لا يوجد مستخدم مطابق
    }
}
