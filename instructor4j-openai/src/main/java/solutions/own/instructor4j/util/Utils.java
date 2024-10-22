package solutions.own.instructor4j.util;

import java.util.List;
import java.util.Map;

/**
 * Utility class containing helper methods.
 */
public class Utils {

    /**
     * Maps Java types to JSON Schema types.
     *
     * @param clazz The Java class.
     * @return The JSON Schema type as a string.
     */
    public static String getJsonType(Class<?> clazz) {
        if (clazz == String.class) {
            return "string";
        } else if (clazz == Integer.class || clazz == int.class) {
            return "integer";
        } else if (clazz == Double.class || clazz == double.class || clazz == Float.class || clazz == float.class) {
            return "number";
        } else if (clazz == Boolean.class || clazz == boolean.class) {
            return "boolean";
        } else if (List.class.isAssignableFrom(clazz)) {
            return "array";
        } else if (Map.class.isAssignableFrom(clazz) || clazz == Object.class) {
            return "object";
        } else {
            // For custom objects, treat as an object
            return "object";
        }
    }

    /**
     * Returns the given value if it is not {@code null}, otherwise returns the given default value.
     * @param <T> The type of the value.
     * @param value The value to return if it is not {@code null}.
     * @param defaultValue The value to return if the value is {@code null}.
     * @return the given value if it is not {@code null}, otherwise returns the given default value.
     */
    public static <T> T getOrDefault(T value, T defaultValue) {
        return value != null ? value : defaultValue;
    }
}
