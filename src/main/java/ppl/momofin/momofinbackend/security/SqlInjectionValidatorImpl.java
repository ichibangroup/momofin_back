package ppl.momofin.momofinbackend.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.util.regex.Pattern;

@Component
public class SqlInjectionValidatorImpl implements SqlInjectionValidator {
    private static final Logger logger = LoggerFactory.getLogger(SqlInjectionValidatorImpl.class);

    // Updated pattern to be more precise and allow apostrophes in business names
    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
            ".*((--)|(/\\*)|(%22)|(%27)|(%2F%2A)|(%23)|(%3B)|(;)).*"
    );

    private static final String[] SQL_KEYWORDS = {
            "SELECT ", "INSERT ", "UPDATE ", "DELETE ", "DROP ", "UNION ",
            "CREATE ", "ALTER ", "TRUNCATE ", " OR ", "WHERE "
    };

    @Override
    public boolean containsSqlInjection(String input) {
        if (input == null) {
            return false;
        }

        String upperInput = " " + input.toUpperCase() + " ";

        // Check for SQL keywords with word boundaries
        for (String keyword : SQL_KEYWORDS) {
            if (upperInput.contains(keyword.toUpperCase())) {
                logger.warn("SQL injection attempt detected with keyword: {}", keyword.trim());
                return true;
            }
        }

        // Check for SQL injection patterns
        if (SQL_INJECTION_PATTERN.matcher(input).matches()) {
            logger.warn("SQL injection attempt detected with pattern matching");
            return true;
        }

        return false;
    }
}
