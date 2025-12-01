package service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

import model.User;

public class LoginService {

    // --------- LOGIN with file ---------
    public User login(String username, String password) {
        String filePath = "src/main/resources/users.txt";

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;

            while ((line = br.readLine()) != null) {
                // نستخدم الميثود الجديدة للقراءة من User model
                User user = User.fromFileString(line);

                if (user != null) {
                    if (user.getName().equals(username) && user.getPassword().equals(password)) {
                        return user; // نجاح تسجيل الدخول
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("❌ ERROR: Cannot read users file!");
        }

        return null; // لا يوجد مستخدم مطابق
    }
}