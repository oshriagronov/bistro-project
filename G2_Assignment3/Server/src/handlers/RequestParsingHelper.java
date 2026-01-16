package handlers;

/**
 * Utility class for parsing primitive values from request payloads.
 * <p>
 * This class centralizes common request parsing logic used by handlers.
 * </p>
 */
public final class RequestParsingHelper {

    // Prevent instantiation
    private RequestParsingHelper() {
    }

    /**
     * Attempts to parse an integer value from the given request payload.
     * <p>
     * Supported input types:
     * <ul>
     *   <li>{@link Integer}</li>
     *   <li>{@link Number}</li>
     *   <li>{@link String} (trimmed, numeric)</li>
     * </ul>
     * </p>
     *
     * @param data the request payload
     * @return the parsed integer, or {@code -1} if parsing fails
     */
    public static int handleStringRequest(Object data) {
        if (data == null) {
            return -1;
        }

        if (data instanceof Integer) {
            return (Integer) data;
        }

        if (data instanceof Number) {
            return ((Number) data).intValue();
        }

        if (data instanceof String) {
            try {
                return Integer.parseInt(((String) data).trim());
            } catch (NumberFormatException e) {
                return -1;
            }
        }

        return -1;
    }
}
