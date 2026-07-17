package integration.api;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

final class ApiDateParser {

    private static final String[] DISPLAY_TIMESTAMP_PATTERNS = {
        "MMM d, yyyy, h:mm:ss a",
        "MMM dd, yyyy, h:mm:ss a"
    };

    private ApiDateParser() {
    }

    static Date toDate(String value) {
        Timestamp timestamp = toTimestamp(value);
        return timestamp == null ? null : new Date(timestamp.getTime());
    }

    static Timestamp toTimestamp(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        String normalized = value.trim().replace('T', ' ');
        try {
            return Timestamp.valueOf(normalized.length() >= 19 ? normalized.substring(0, 19) : normalized + " 00:00:00");
        } catch (IllegalArgumentException ex) {
            // Gson serializes java.sql dates as locale text in some API responses.
        }

        for (String pattern : DISPLAY_TIMESTAMP_PATTERNS) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.US);
                sdf.setLenient(false);
                return new Timestamp(sdf.parse(value.trim()).getTime());
            } catch (ParseException ex) {
                // Try next known API date format.
            }
        }
        return null;
    }
}
