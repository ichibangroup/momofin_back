package ppl.momofin.momofinbackend.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.util.regex.Pattern;

@Component
public class SqlInjectionValidatorImpl implements SqlInjectionValidator {
    private static final Logger logger = LoggerFactory.getLogger(SqlInjectionValidatorImpl.class);

    // Define maximum input length to prevent ReDoS
    private static final int MAX_INPUT_LENGTH = 1000;

    // Split patterns for better control and security
    private static final Pattern SQL_KEYWORDS_PATTERN = Pattern.compile(
            "\\b(SELECT|INSERT|UPDATE|DELETE|DROP|UNION|CREATE|ALTER|TRUNCATE|SCHEMA|GRANT|REVOKE)\\b(?!\\w)",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern SQL_COMMENTS_PATTERN = Pattern.compile(
            "(--.*)|(\\*/)|(^/\\*)|(/\\*$)"
    );

    private static final Pattern SQL_INJECTION_CHARS_PATTERN = Pattern.compile(
            "(';)|(^--)|(%27)|(\\+27)|(%3B)|(;\\s*$)|('\\s+OR\\s+')"
    );

    @Override
    public boolean containsSqlInjection(String input) {
        if (input == null) {
            return false;
        }

        // Prevent ReDoS with length check
        if (input.length() > MAX_INPUT_LENGTH) {
            logger.warn("Input exceeds maximum length");
            return true;
        }

        // Check for SQL keywords with proper word boundaries
        if (SQL_KEYWORDS_PATTERN.matcher(input).find()) {
            logger.warn("SQL injection attempt detected: SQL keyword found");
            return true;
        }

        // Check for SQL comments (but allow # in addresses)
        if (SQL_COMMENTS_PATTERN.matcher(input).find()) {
            logger.warn("SQL injection attempt detected: Comment pattern found");
            return true;
        }

        // Check for SQL injection character sequences
        if (SQL_INJECTION_CHARS_PATTERN.matcher(input).find()) {
            logger.warn("SQL injection attempt detected: Suspicious character sequence found");
            return true;
        }

        return false;
    }
}