package model;

public class User {

    private String name;
    private String password;
    private String role;

    private double outstandingFine;  // ← الغرامة المتراكمة على المستخدم

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

    // إضافة غرامة على المستخدم (مثل: 1 شيكل لكل يوم تأخير)
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
        }

        outstandingFine -= amount;

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
