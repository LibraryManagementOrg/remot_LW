package model;

/**
 * Represents a CD in the library.
 * Extends Media and uses CDFineStrategy.
 * 
 * @author Student
 */
public class CD extends media {

    public CD(String title, String artist, String barcode) {
        // نمرر البيانات للأب (Media)
        // Creator = Artist, ID = Barcode
        super(title, artist, barcode);
        
        // 🌟 تعيين استراتيجية الغرامة الخاصة بالسي دي (20 شيكل)
        this.setFineStrategy(new CDFineStrategy());
    }

    /**
     * تنفيذ دالة مدة الإعارة.
     * حسب US5.1: مدة استعارة السي دي 7 أيام فقط.
     */
    @Override
    public int getLoanPeriod() {
        return 7;
    }

    /**
     * قيمة الغرامة اليومية (لأغراض العرض أو الاحتياط).
     */
    @Override
    public double getDailyFine() {
        return 20.0;
    }
    
    // ===== Getters for clarity =====
    
    public String getArtist() {
        return super.getCreator(); // في السي دي، الـ Creator هو الفنان
    }
    
    public String getBarcode() {
        return super.getId(); // في السي دي، الـ ID هو الباركود
    }

    @Override
    public String toString() {
        return "CD: " + getTitle() + " | Artist: " + getArtist() + " | Barcode: " + getBarcode();
    }
}