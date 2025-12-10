package modeltest;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import model.Admin;

class AdminTest {

    @Test
    @DisplayName("Test Constructor and Getters")
    void testAdminInitialization() {
        // Arrange
        String username = "adminUser";
        String password = "adminPassword";

        // Act
        Admin admin = new Admin(username, password);

        // Assert
        assertNotNull(admin);
        assertEquals(username, admin.getUsername());
        assertEquals(password, admin.getPassword());
    }

    // 👇 هذا الجزء مهم جداً لزيادة الكافريج إذا كان عندك دوال Setters 👇
    /*
    @Test
    @DisplayName("Test Setters")
    void testSetters() {
        // Arrange
        Admin admin = new Admin("oldUser", "oldPass");

        // Act
        admin.setUsername("newUser");
        admin.setPassword("newPass");

        // Assert
        assertEquals("newUser", admin.getUsername());
        assertEquals("newPass", admin.getPassword());
    }
    */

    // 👇 هذا التست عشان يغطي دالة toString إذا كانت موجودة (غالباً بتنسيها) 👇
    /*
    @Test
    @DisplayName("Test toString method")
    void testToString() {
        Admin admin = new Admin("user", "pass");
        String result = admin.toString();
        
        assertNotNull(result);
        assertTrue(result.contains("user"));
    }
    */
    
    @Test
    @DisplayName("Test Edge Cases (Nulls and Empty)")
    void testEdgeCases() {
        Admin emptyAdmin = new Admin("", "");
        assertEquals("", emptyAdmin.getUsername());
        assertEquals("", emptyAdmin.getPassword());

        Admin nullAdmin = new Admin(null, null);
        assertNull(nullAdmin.getUsername());
        assertNull(nullAdmin.getPassword());
    }
}