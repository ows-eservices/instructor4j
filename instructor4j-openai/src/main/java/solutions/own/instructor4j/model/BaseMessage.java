package solutions.own.instructor4j.model;
import java.io.Serializable;

/**
 * Represents a basic message in a chat system, encapsulating the role of the message sender
 * and the content of the message.
 * <p>
 * Instances of this class can be used to model different types of messages such as system prompts,
 * user inputs, assistant responses, function calls, and tool interactions within a conversational AI context.
 * </p>
 *
 * <p><b>Example Usage:</b></p>
 * <pre>{@code
 * // Creating a user message
 * BaseMessage userMessage = new BaseMessage(BaseMessage.Role.USER.getValue(), "Hello, how are you?");
 *
 * // Creating an assistant message
 * BaseMessage assistantMessage = new BaseMessage(BaseMessage.Role.ASSISTANT.getValue(), "I'm good, thank you!");
 * }</pre>
 *
 * <p><b>Thread Safety:</b> This class is not thread-safe. If multiple threads access an instance concurrently,
 * and at least one of the threads modifies it, external synchronization is required.</p>
 */
public class BaseMessage implements Serializable {

    /**
     * The role of the message sender. Valid roles are defined in the {@link Role} enum.
     * <p>
     * Examples of roles include "system", "user", "assistant", "function", and "tool".
     * </p>
     */
    private String role;

    /**
     * The content of the message. This is the actual text or payload that the sender conveys.
     * <p>
     * The content should be a meaningful string that adheres to the context defined by the role.
     * </p>
     */
    private String content;

    /**
     * Default constructor for a new {@code BaseMessage} instance with no initial role or content.
     */
    public BaseMessage() {
    }

    /**
     * Constructs a new {@code BaseMessage} instance with the specified role and content.
     *
     * @param role    the role of the message sender; must correspond to one of the predefined roles in {@link Role}
     * @param content the content of the message; must not be {@code null}
     * @throws IllegalArgumentException if {@code role} does not correspond to any defined roles in {@link Role}
     * @throws NullPointerException     if {@code content} is {@code null}
     */
    public BaseMessage(String role, String content) {
        if (role == null) {
            throw new NullPointerException("Role cannot be null.");
        }
        Role validatedRole = Role.fromString(role);
        if (validatedRole == null) {
            throw new IllegalArgumentException("Invalid role: " + role);
        }
        if (content == null) {
            throw new NullPointerException("Content cannot be null.");
        }
        this.role = role;
        this.content = content;
    }

    /**
     * Retrieves the role of the message sender.
     *
     * @return the role of the sender as a {@code String}
     */
    public String getRole() {
        return role;
    }

    /**
     * Sets the role of the message sender.
     *
     * @param role the new role to assign to the sender; must correspond to one of the predefined roles in {@link Role}
     * @throws IllegalArgumentException if {@code role} does not correspond to any defined roles in {@link Role}
     * @throws NullPointerException     if {@code role} is {@code null}
     */
    public void setRole(String role) {
        if (role == null) {
            throw new NullPointerException("Role cannot be null.");
        }
        Role validatedRole = Role.fromString(role);
        if (validatedRole == null) {
            throw new IllegalArgumentException("Invalid role: " + role);
        }
        this.role = role;
    }

    /**
     * Retrieves the content of the message.
     *
     * @return the message content as a {@code String}
     */
    public String getContent() {
        return content;
    }

    /**
     * Sets the content of the message.
     *
     * @param content the new content to assign to the message; must not be {@code null}
     * @throws NullPointerException if {@code content} is {@code null}
     */
    public void setContent(String content) {
        if (content == null) {
            throw new NullPointerException("Content cannot be null.");
        }
        this.content = content;
    }

    /**
     * Enumerates the possible roles that a message sender can have within the chat system.
     * <p>
     * Each enum constant corresponds to a specific type of participant or system component in the conversation.
     * </p>
     */
    public enum Role {
        /**
         * Represents a system-level message, typically used for setting context or providing system notifications.
         */
        SYSTEM("system"),

        /**
         * Represents a message from a user participant.
         */
        USER("user"),

        /**
         * Represents a message from an assistant or AI participant.
         */
        ASSISTANT("assistant"),

        /**
         * Represents a message invoking a function, possibly for performing specific tasks or operations.
         */
        FUNCTION("function"),

        /**
         * Represents a message interacting with a tool or external system.
         */
        TOOL("tool");

        /**
         * The string value associated with the role, used for serialization or comparison.
         */
        private final String value;

        /**
         * Constructs a {@code Role} enum constant with the specified string value.
         *
         * @param value the string representation of the role
         */
        Role(String value) {
            this.value = value;
        }

        /**
         * Retrieves the string value associated with the role.
         *
         * @return the string representation of the role
         */
        public String getValue() {
            return this.value;
        }

        /**
         * Converts a string to its corresponding {@code Role} enum constant.
         *
         * @param value the string representation of the role
         * @return the matching {@code Role} enum constant, or {@code null} if no match is found
         */
        public static Role fromString(String value) {
            if (value == null) {
                return null;
            }
            for (Role role : Role.values()) {
                if (role.value.equalsIgnoreCase(value)) {
                    return role;
                }
            }
            return null;
        }
    }
}
