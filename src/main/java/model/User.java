package model;

public class User {

    private String name;
    private String password;
    private String role;
    private double outstandingFine;  // الغرامة المتراكمة على المستخدم
    
    // ✅ (جديد) حقل الإيميل
    private String email; 

    // ✅ الكونستركتور القديم (تم إبقاؤه لضمان عدم تعطل الأكواد القديمة)
    // يقوم بوضع قيمة افتراضية للإيميل والغرامة
    public User(String name, String password, String role) {
        this.name = name;
        this.password = password;
        this.role = role;
        this.outstandingFine = 0.0;
        this.email = ""; // قيمة فارغة افتراضية لتجنب الاخطاء
    }

    // ✅ (جديد) كونستركتور كامل
    // استخدمي هذا عند قراءة البيانات من ملف users.txt لأن الملف يحتوي على الغرامة والإيميل
    public User(String name, String password, String role, double outstandingFine, String email) {
        this.name = name;
        this.password = password;
        this.role = role;
        this.outstandingFine = outstandingFine;
        this.email = email;
    }

    // ===== GETTERS =====
    public String getName() { return name; }
    public String getPassword() { return password; }
    public String getRole() { return role; }
    public double getOutstandingFine() { return outstandingFine; }
    
    // ✅ (جديد)
    public String getEmail() { return email; }

    // ===== SETTERS =====
    public void setOutstandingFine(double outstandingFine) {
        this.outstandingFine = outstandingFine;
    }
    
    // ✅ (جديد)
    public void setEmail(String email) {
        this.email = email;
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
                ", email='" + email + '\'' + // ✅ تمت إضافة الايميل للطباعة
                '}';
    }
}