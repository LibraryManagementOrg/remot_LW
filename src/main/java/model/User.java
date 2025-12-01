package model;

public class User {
    private String name;
    private String email; 
    private String password;
    private String role;
    private double outstandingFine;

    // الكونستركتور
    public User(String name, String email, String password, String role) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.outstandingFine = 0.0;
    }

    // Getters & Setters
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getRole() { return role; }
    public double getOutstandingFine() { return outstandingFine; }
    
    public void setOutstandingFine(double f) { this.outstandingFine = f; }

    // ==========================================
    // ✅✅ الدوال المنطقية (كانت ناقصة عندك) ✅✅
    // ==========================================
    
    // هل يسمح له بالاستعارة؟ (فقط إذا لم يكن عليه غرامات)
    public boolean canBorrow() {
        return outstandingFine == 0.0;
    }

    // دفع الغرامة
    public void payFine(double amount) {
        if (amount > 0) {
            outstandingFine -= amount;
            if (outstandingFine < 0) outstandingFine = 0;
        }
    }

    // ==========================================
    //          حفظ وقراءة الملفات
    // ==========================================
    
    public String toFileString() {
        return name + ";" + email + ";" + password + ";" + role + ";" + outstandingFine;
    }

    public static User fromFileString(String line) {
        String[] parts = line.split(";", -1);
        if (parts.length >= 4) {
            User u = new User(parts[0], parts[1], parts[2], parts[3]);
            if (parts.length > 4 && !parts[4].isEmpty()) {
                try {
                    u.setOutstandingFine(Double.parseDouble(parts[4]));
                } catch (NumberFormatException e) {
                    u.setOutstandingFine(0.0);
                }
            }
            return u;
        }
        return null;
    }
    
    @Override
    public String toString() {
        return "User: " + name + " (" + email + ")";
    }
}