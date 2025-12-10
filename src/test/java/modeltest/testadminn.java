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

        // Act (يغطي الباني ودوائل Getters)
        Admin admin = new Admin(expectedUsername, expectedPassword);

        // Assert
        assertAll("Valid Data Check",
            () -> assertNotNull(admin, "Admin object should be created"),
            () -> assertEquals(expectedUsername, admin.getUsername(), "Username should match"),
            () -> assertEquals(expectedPassword, admin.getPassword(), "Password should match")
        );
    }

    @Test
    @DisplayName("Test Admin with empty strings")
    void testAdminWithEmptyData() {
        // Act (يغطي الباني ودوائل Getters)
        Admin admin = new Admin("", "");
        
        // Assert
        assertAll("Empty Strings Check",
            () -> assertEquals("", admin.getUsername()),
            () -> assertEquals("", admin.getPassword())
        );
    }

    @Test
    @DisplayName("Test Admin with null values")
    void testAdminWithNulls() {
        // Act (يغطي الباني ودوائل Getters)
        Admin admin = new Admin(null, null);
        
        // Assert
        assertAll("Null Values Check",
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

        // Act (يغطي الباني ودوائل Getters)
        Admin admin = new Admin(userWithSpace, passWithChars);

        // Assert
        assertAll("Special Characters Check",
            () -> assertEquals(userWithSpace, admin.getUsername()),
            () -> assertEquals(passWithChars, admin.getPassword())
        );
    }
}