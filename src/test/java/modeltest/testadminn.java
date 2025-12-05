package modeltest;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import model.Admin;



class testadminn {

    @Test
    @DisplayName("Test Constructor and Getters with valid data")
    void testAdminCreation() {
        // ترتيب البيانات (Arrange)
        String expectedUsername = "superAdmin";
        String expectedPassword = "securePassword123";

        // تنفيذ الفعل (Act)
        Admin admin = new Admin(expectedUsername, expectedPassword);

        // التحقق من النتيجة (Assert)
        assertEquals(expectedUsername, admin.getUsername(), "Username should match the input");
        assertEquals(expectedPassword, admin.getPassword(), "Password should match the input");
    }

    @Test
    @DisplayName("Test Admin with empty strings")
    void testAdminWithEmptyData() {
        Admin admin = new Admin("", "");
        
        assertEquals("", admin.getUsername());
        assertEquals("", admin.getPassword());
    }

    @Test
    @DisplayName("Test Admin with null values")
    void testAdminWithNulls() {
        // بما أن الكود الخاص بك لا يمنع القيم الفارغة (null)
        // يجب التأكد من أنه يقبلها ويخزنها كما هي دون مشاكل
        Admin admin = new Admin(null, null);
        
        assertNull(admin.getUsername(), "Username should be null");
        assertNull(admin.getPassword(), "Password should be null");
    }
}