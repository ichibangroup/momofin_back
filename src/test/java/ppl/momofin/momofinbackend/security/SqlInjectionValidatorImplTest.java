package ppl.momofin.momofinbackend.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SqlInjectionValidatorImplTest {

    private SqlInjectionValidator validator;

    @BeforeEach
    void setUp() {
        validator = new SqlInjectionValidatorImpl();
    }

    @Test
    void containsSqlInjection_WithCommonAttacks() {
        String[] attacks = {
                "'; DROP TABLE users--",             // SQL injection with comment
                "admin';--",                         // Comment attack
                "' UNION SELECT * FROM users--",     // Union attack
                "'; INSERT INTO users VALUES ('hack','hack')", // Insert attack
                "'; DELETE FROM organizations;",     // Delete attack
                "SELECT * FROM users",               // Direct SQL query
                "DROP TABLE organizations",          // Direct drop command
                "'; UPDATE users SET admin='true'"   // Update attack
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
        assertTrue(validator.containsSqlInjection("Note: /*comment*/"));
        assertTrue(validator.containsSqlInjection("Comment: --drop table"));
    }

    @Test
    void containsSqlInjection_WithMixedCaseKeywords() {
        assertTrue(validator.containsSqlInjection("SeLeCt * FROM users"));
        assertTrue(validator.containsSqlInjection("DrOp TABLE users"));
        assertTrue(validator.containsSqlInjection("inserT INTO users"));
        assertTrue(validator.containsSqlInjection("DELETE from Users"));
    }
    @Test
    void containsSqlInjection_WithMaxLengthExceeded() {
        // Create a string that exceeds MAX_INPUT_LENGTH (1000)
        StringBuilder longInput = new StringBuilder();
        for (int i = 0; i < 1001; i++) {
            longInput.append("a");
        }

        assertTrue(validator.containsSqlInjection(longInput.toString()),
                "Should detect input exceeding maximum length");
    }

    @Test
    void containsSqlInjection_WithSuspiciousCharacterSequences() {
        String[] suspiciousInputs = {
                "username' OR '1'='1",        // OR condition injection
                "data'; DROP TABLE users",    // Multiple statement injection
                "value%27 OR %271%27=%271",   // URL encoded injection
                "field'; SELECT * FROM users", // Chain injection
                "input+27",                   // Alternative encoding
                "command; SELECT * FROM users" // Command injection
        };

        for (String input : suspiciousInputs) {
            assertTrue(validator.containsSqlInjection(input),
                    "Should detect suspicious character sequence: " + input);
        }
    }
}