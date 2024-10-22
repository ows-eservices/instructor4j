package solutions.own.instructor4j.config;

import static solutions.own.instructor4j.util.Utils.getOrDefault;

/**
 * Utility class for managing API keys, specifically for OpenAI.
 * It provides methods to retrieve environment variables related to the API keys.
 */
public class ApiKeys {

    /**
     * The default value when the OpenAI API key is not provided.
     */
    public static final String OPENAI_API_KEY_NOT_PROVIDED = "none";

    /**
     * The OpenAI API key, retrieved from the environment or set to the default value
     * if the key is not found.
     */
    public static final String OPENAI_API_KEY = getOrDefault(getEnv("OPENAI_API_KEY"), OPENAI_API_KEY_NOT_PROVIDED);

    /**
     * Retrieves the value of an environment variable by the given key.
     *
     * @param key The name of the environment variable to retrieve.
     * @return The value of the environment variable, or {@code null} if not set.
     */
    protected static String getEnv(String key) {
        return System.getenv(key);
    }

    /**
     * Returns the provided value if it is not {@code null}, otherwise returns the default value.
     *
     * @param <T> The type of the value.
     * @param value The value to check.
     * @param defaultValue The default value to return if {@code value} is {@code null}.
     * @return The provided value if not {@code null}, otherwise the default value.
     */
    public static <T> T getOrDefault(T value, T defaultValue) {
        return value != null ? value : defaultValue;
    }
}