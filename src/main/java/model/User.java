package model;

public class User {
<<<<<<< HEAD
    private String name;
    private String email; 
    private String password;
    private String role;
    private double outstandingFine;
=======

private String name;
private String password;
private String role;
>>>>>>> branch 'master' of https://github.com/layalqaradeh/remot_LW.git

<<<<<<< HEAD
    // الكونستركتور
    public User(String name, String email, String password, String role) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.outstandingFine = 0.0;
=======
private double outstandingFine;  // الغرامة المتراكمة على المستخدم

public User(String name, String password, String role) {
    this.name = name;
    this.password = password;
    this.role = role;
    this.outstandingFine = 0.0;  // يبدأ بدون غرامات
}

// ===== GETTERS =====
public String getName() { return name; }
public String getPassword() { return password; }
public String getRole() { return role; }
public double getOutstandingFine() { return outstandingFine; }

// ===== SETTERS =====
public void setOutstandingFine(double outstandingFine) {
    this.outstandingFine = outstandingFine;
}

// ===== LOGIC =====

// إضافة غرامة على المستخدم
public void addFine(double amount) {
    if (amount > 0) {
        outstandingFine += amount;
    }
}

// دفع غرامة (جزئي أو كامل)
public void payFine(double amount) {
    if (amount <= 0) {
        System.out.println("❌ Invalid amount!");
        return;
>>>>>>> branch 'master' of https://github.com/layalqaradeh/remot_LW.git
    }

<<<<<<< HEAD
    // Getters & Setters
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getRole() { return role; }
    public double getOutstandingFine() { return outstandingFine; }
    
    public void setOutstandingFine(double f) { this.outstandingFine = f; }
=======
    outstandingFine -= amount;
>>>>>>> branch 'master' of https://github.com/layalqaradeh/remot_LW.git

<<<<<<< HEAD
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
=======
    if (outstandingFine < 0) {
        outstandingFine = 0; // لا نسمح برصيد سلبي
    }
}

// هل يمكنه الاستعارة؟
public boolean canBorrow() {
    return outstandingFine == 0.0;
}

@Override
public String toString() {
    return "User {" +
            "name='" + name + '\'' +
            ", role='" + role + '\'' +
            ", outstandingFine=" + outstandingFine +
            '}';
}


}
>>>>>>> branch 'master' of https://github.com/layalqaradeh/remot_LW.git
