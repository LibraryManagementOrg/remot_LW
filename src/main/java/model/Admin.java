package model;

import java.util.Objects; // يجب استيراد هذه الحزمة لدوال equals/hashCode

public class Admin {
	private String username;
    private String password;

    public Admin(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
    
    // --- إضافة دوال equals و hashCode لزيادة Condition Coverage ---
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        // شرطان (o == null) و (getClass() != o.getClass())
        if (o == null || getClass() != o.getClass()) return false; 
        
        Admin admin = (Admin) o;
        
        // شرطان إضافيان (Objects.equals(username, admin.username)) و (Objects.equals(password, admin.password))
        return Objects.equals(username, admin.username) && Objects.equals(password, admin.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, password);
    }
}