package modeltest;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import model.Admin;

class testadminn {

    @Test
    @DisplayName("Test Constructor and Getters with valid data")
    void testAdminCreation() {
        // Arrange
        String expectedUsername = "superAdmin";
        String expectedPassword = "securePassword123";

        // Act
        Admin admin = new Admin(expectedUsername, expectedPassword);

        // Assert
        assertAll("Valid Data",
            () -> assertNotNull(admin, "Admin object should be created"),
            () -> assertEquals(expectedUsername, admin.getUsername(), "Username should match"),
            () -> assertEquals(expectedPassword, admin.getPassword(), "Password should match")
        );
    }

    @Test
    @DisplayName("Test Admin with empty strings")
    void testAdminWithEmptyData() {
        // Act
        Admin admin = new Admin("", "");
        
        // Assert
        assertAll("Empty Strings",
            () -> assertEquals("", admin.getUsername()),
            () -> assertEquals("", admin.getPassword())
        );
    }

    @Test
    @DisplayName("Test Admin with null values")
    void testAdminWithNulls() {
        // Act
        Admin admin = new Admin(null, null);
        
        // Assert
        assertAll("Null Values",
            () -> assertNull(admin.getUsername(), "Username should be null"),
            () -> assertNull(admin.getPassword(), "Password should be null")
        );
    }

    @Test
    @DisplayName("Test Admin with special characters and spaces")
    void testAdminWithSpecialChars() {
        // Arrange
        String userWithSpace = "User Name";
        String passWithChars = "Pass@123 #!";

        // Act
        Admin admin = new Admin(userWithSpace, passWithChars);

        // Assert
        assertAll("Special Characters",
            () -> assertEquals(userWithSpace, admin.getUsername()),
            () -> assertEquals(passWithChars, admin.getPassword())
        );
    }
    
    // -------------------------------------------------------------------
    // اختبارات جديدة لتغطية دوال equals و hashCode
    // -------------------------------------------------------------------

    @Test
    @DisplayName("Test equals method for true equality and matching hash codes")
    void testEqualsTrue() {
        Admin admin1 = new Admin("userA", "pass123");
        Admin admin2 = new Admin("userA", "pass123");
        
        // 1. التغطية: يجب أن يكونوا متساويين
        assertTrue(admin1.equals(admin2), "Two admins with same credentials should be equal");
        
        // 2. التغطية: الكائن يساوي نفسه (self-equality)
        assertTrue(admin1.equals(admin1), "Admin should equal itself");
        
        // 3. التغطية: التأكد من أن hash codes متساوية للكائنات المتساوية
        assertEquals(admin1.hashCode(), admin2.hashCode(), "Hash codes must be equal if objects are equal");
    }

    @Test
    @DisplayName("Test equals method for false inequality (covering all false conditions)")
    void testEqualsFalse() {
        Admin admin1 = new Admin("userA", "pass123");
        
        // 1. التغطية: المقارنة مع كائن null
        assertFalse(admin1.equals(null), "Admin should not equal null"); // يغطي الشرط (o == null)
        
        // 2. التغطية: المقارنة مع كائن من نوع مختلف
        assertFalse(admin1.equals("A string"), "Admin should not equal different object type"); // يغطي الشرط (getClass() != o.getClass())
        
        // 3. التغطية: اختلاف في اسم المستخدم
        Admin admin2 = new Admin("userB", "pass123");
        assertFalse(admin1.equals(admin2), "Admins with different usernames should not be equal");
        
        // 4. التغطية: اختلاف في كلمة المرور
        Admin admin3 = new Admin("userA", "pass456");
        assertFalse(admin1.equals(admin3), "Admins with different passwords should not be equal");
    }
}