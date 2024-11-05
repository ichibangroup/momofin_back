package ppl.momofin.momofinbackend.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SqlInjectionValidatorImplTest {

    private SqlInjectionValidator validator;

    @BeforeEach
    void setUp() {
        validator = new SqlInjectionValidatorImpl();
    }

    @Test
    void containsSqlInjection_WithCommonAttacks() {
        String[] attacks = {
                "' OR '1'='1",                              // Basic SQL injection
                "'; DROP TABLE users--",                    // Drop table attack
                "admin'--",                                 // Comment attack
                "' UNION SELECT * FROM users--",            // Union attack
                "'; INSERT INTO users VALUES ('hack','hack')--", // Insert attack
                "company'; DELETE FROM organizations;--",    // Delete attack
                "user' OR 'x'='x",                         // Always true condition
                "SELECT * FROM users",                      // Direct SQL query
                "DROP TABLE organizations",                 // Direct drop command
                "'; UPDATE users SET admin='true'--"        // Update attack
        };

        for (String attack : attacks) {
            assertTrue(validator.containsSqlInjection(attack),
                    "Should detect attack: " + attack);
        }
    }

    @Test
    void containsSqlInjection_WithValidBusinessInputs() {
        String[] validInputs = {
                "John's Hardware Store",           // Apostrophe in business name
                "Tech & Digital Solutions",        // Ampersand in name
                "Company (Asia) Ltd.",            // Parentheses in name
                "First-Class Services",           // Hyphen in name
                "123 Main St, Suite #100",        // Address format
                "O'Connor & Sons Trading",        // Name with apostrophe and ampersand
                "Tech.Co",                        // Name with dot
                "20th Century Solutions",         // Name with number
                "Smith & Co. (International)",    // Complex business name
                "New-York Based Company"          // Name with hyphen
        };

        for (String input : validInputs) {
            assertFalse(validator.containsSqlInjection(input),
                    "Should allow valid input: " + input);
        }
    }

    @Test
    void containsSqlInjection_WithNullAndEmptyInputs() {
        assertFalse(validator.containsSqlInjection(null),
                "Null input should not be flagged as SQL injection");
        assertFalse(validator.containsSqlInjection(""),
                "Empty string should not be flagged as SQL injection");
        assertFalse(validator.containsSqlInjection("   "),
                "Whitespace should not be flagged as SQL injection");
    }

    @Test
    void containsSqlInjection_WithSpecialCharacters() {
        // These should be valid
        assertFalse(validator.containsSqlInjection("Cost: $100.00"));
        assertFalse(validator.containsSqlInjection("Temperature: 20°C"));
        assertFalse(validator.containsSqlInjection("Notes: !@#$%^&*()"));

        // These should be detected as SQL injection
        assertTrue(validator.containsSqlInjection("Value: '; DROP TABLE;"));
        assertTrue(validator.containsSqlInjection("Note: /**/DELETE FROM users"));
        assertTrue(validator.containsSqlInjection("Comment: --drop table"));
    }

    @Test
    void containsSqlInjection_WithMixedCaseKeywords() {
        assertTrue(validator.containsSqlInjection("SeLeCt * FROM users"));
        assertTrue(validator.containsSqlInjection("DrOp TABLE users"));
        assertTrue(validator.containsSqlInjection("inserT INTO users"));
        assertTrue(validator.containsSqlInjection("DELETE from Users"));
    }
}