package solutions.own.instructor4j.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.openai.core.JsonValue;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import solutions.own.instructor4j.exception.InstructorException;

/**
 * Utility class containing helper methods.
 */
public class Utils {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Parses a JSON string into an instance of the specified response model.
     *
     * @param json          the JSON string to parse.
     * @param responseModel the class of the response model.
     * @param <T>           the type of the response model.
     * @return an instance of {@code T} populated with data from the JSON string.
     * @throws InstructorException if parsing fails.
     */
    public static <T> T parseJson(String json, Class<T> responseModel) throws InstructorException {
        try {
            return objectMapper.readValue(json, responseModel);
        } catch (JsonProcessingException e) {
            throw new InstructorException("Failed to parse JSON: " + e.getMessage(), e);
        }
    }

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

    /**
     * Removes all escape characters (backslashes before quotes) from the JSON string using replaceAll.
     *
     * @param input the original JSON string with escape characters
     * @return the unescaped JSON string
     */
    public static String removeAllEscapedQuotes(String input) {
        if (input == null) {
            return null;
        }
        return input.replaceAll("\\\\\"", "\"");
    }

    /**
     * Ensures that the given JSON-like string is syntactically closed with respect to quotes, curly braces,
     * and square brackets. This method attempts to balance unclosed brackets and quotes by appending
     * the necessary closing characters at the end of the string.
     *
     * <p>The method will:
     * <ul>
     *   <li>Count and ensure that if there's an odd number of double quotes, an extra quote will be appended
     *   at the end to balance them.</li>
     *   <li>Track the nesting of '{' and '[' characters using a stack. For each unmatched opening bracket,
     *   it appends the corresponding closing bracket ('}' or ']') at the end of the string.</li>
     * </ul>
     *
     * <p>Note: This method does not guarantee that the resulting string is fully valid JSON. It only attempts
     * to close opened quotes and brackets. If the initial string was malformed beyond missing closing brackets
     * or quotes, the result may still not be valid JSON.</p>
     *
     * @param json the input string to ensure closures for quotes and brackets
     * @return the adjusted string with appended closing characters if necessary
     */
    public static String ensureJsonClosures(String json) {
        StringBuilder result = new StringBuilder(json);
        java.util.Deque<Character> stack = new java.util.ArrayDeque<>();

        int numOfQuotes = 0;
        for (char c : json.toCharArray()) {
            switch (c) {
                case '"':
                    numOfQuotes++;
                    break;
                default:
                    break;
            }
        }

        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '{' || c == '[') {
                stack.push(c);
            } else if (c == '}' || c == ']') {
                if (!stack.isEmpty()) {
                    stack.pop();
                }
            }
        }

        if (numOfQuotes > 0 && numOfQuotes % 2 != 0) {
            result.append('"');
        }

        while (!stack.isEmpty()) {
            char opened = stack.pop();
            if (opened == '{') {
                result.append('}');
            } else if (opened == '[') {
                result.append(']');
            }
        }

        return result.toString();
    }

    /**
     * Checks whether the given string has balanced quotes, curly braces, and square brackets,
     * indicative of properly closed structures that are necessary (but not sufficient) for valid JSON.
     *
     * <p>This method counts the number of double quotes (") and ensures it is even, implying
     * that quotes are properly paired. It also tracks the nesting of '{' and '[' characters
     * with a stack and ensures every opened bracket is eventually closed by a matching '}' or ']'.
     *
     * <p>Note: This method does not guarantee the string is valid JSON. It only checks for
     * balanced quotes and bracket pairs. Other invalid JSON constructs (e.g., missing commas,
     * unquoted keys, invalid characters) may still be present.
     *
     * @param json the input string to check for balanced quotes and brackets
     * @return true if the number of quotes is even and all brackets are properly closed;
     *         false otherwise
     */
    public static boolean isJsonValidOnClosures(String json) {
        StringBuilder result = new StringBuilder(json);
        java.util.Deque<Character> stack = new java.util.ArrayDeque<>();

        int numOfQuotes = 0;
        for (char c : json.toCharArray()) {
            switch (c) {
                case '"':
                    numOfQuotes++;
                    break;
                default:
                    break;
            }
        }

        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '{' || c == '[') {
                stack.push(c);
            } else if (c == '}' || c == ']') {
                if (!stack.isEmpty()) {
                    stack.pop();
                }
            }
        }

        if (numOfQuotes > 0 && numOfQuotes % 2 != 0) {
            return false;
        }

        return stack.isEmpty();
    }

    /**
     * Extracts and converts a JSON array under a given root element into a list of objects of the specified type.
     *
     * <p>This method:
     * <ul>
     *   <li>Parses the provided JSON string into a {@link JsonNode}.</li>
     *   <li>Retrieves the child node corresponding to the specified {@code rootElement}.</li>
     *   <li>Converts that node into a {@link List} of {@code clazz}-typed objects using Jackson's type conversion.</li>
     * </ul>
     *
     * <p>For example, if the JSON looks like:
     * <pre>
     * {
     *   "data": [
     *     { "name": "John Doe", "email": "john@example.com" },
     *     { "name": "Jane Smith", "email": "jane@example.com" }
     *   ]
     * }
     * </pre>
     * and you call {@code getEntities(json, Participant.class, "data")},
     * this method will return a {@code List<Participant>} populated with the objects from the "data" array.
     *
     * @param json the JSON string from which to extract data
     * @param clazz the target class type to which the list elements will be mapped
     * @param rootElement the name of the root element in the JSON under which the array is located
     * @param <T> the type of the objects to be created and returned in the list
     * @return a list of objects of type {@code T} constructed from the JSON array
     * @throws JsonProcessingException if the JSON cannot be parsed or the conversion fails
     */
    public static <T> List<T> getEntities(String json, Class<T> clazz, String rootElement) throws JsonProcessingException {
        JsonNode root = objectMapper.readTree(json);
        JsonNode dataNode = root.get(rootElement);
        CollectionType listType = objectMapper.getTypeFactory().constructCollectionType(List.class, clazz);
        return objectMapper.convertValue(dataNode, listType);
    }

    /**
     * Returns an immutable list containing only the specified element.
     * <p>
     * This method creates a list that contains exactly one element. The returned list is immutable,
     * meaning that any attempt to modify it (e.g., adding or removing elements) will result in an
     * {@link UnsupportedOperationException}.
     * </p>
     *
     * @param <T>     the type of the element
     * @param element the single element to be contained in the returned list
     * @return an immutable list containing only the specified element
     * @throws NullPointerException if the specified element is {@code null}
     */
    public static <T> List<T> listOf(T element) {
        return Collections.singletonList(element);
    }

    /**
     * Converts a Map&lt;String, Object&gt; to a Map&lt;String, JsonValue&gt;.
     *
     * @param originalMap the original map with String keys and Object values
     * @return a new map with String keys and JsonValue values
     * @throws NullPointerException     if the originalMap or any key is null
     * @throws IllegalArgumentException if any value cannot be converted to JsonValue
     */
    public static Map<String, JsonValue> convertMap(Map<String, Object> originalMap) {
        if (originalMap == null) {
            throw new NullPointerException("Original map cannot be null");
        }

        Map<String, JsonValue> jsonValueMap = new HashMap<>();

        for (Map.Entry<String, Object> entry : originalMap.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (key == null) {
                throw new NullPointerException("Map keys cannot be null");
            }

            JsonValue jsonValue = JsonValue.from(value); // Your existing method
            jsonValueMap.put(key, jsonValue);
        }

        return jsonValueMap;
    }

    /**
     * Converts an Optional to a Stream.
     *
     * @param optional the Optional to convert
     * @param <T>      the type of the Optional's value
     * @return a Stream containing the Optional's value if present, otherwise an empty Stream
     */
    public static <T> Stream<T> optionalToStream(Optional<T> optional) {
        return optional.isPresent() ? Stream.of(optional.get()) : Stream.empty();
    }
}
