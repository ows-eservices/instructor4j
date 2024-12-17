package solutions.own.instructor4j.util;

import com.openai.models.ChatCompletionAssistantMessageParam;
import com.openai.models.ChatCompletionMessageParam;
import com.openai.models.ChatCompletionSystemMessageParam;
import com.openai.models.ChatCompletionUserMessageParam;
import java.util.List;
import java.util.stream.Collectors;
import solutions.own.instructor4j.model.BaseMessage;

public class MessageConverter {

    /**
     * Converts a list of {@link BaseMessage} objects to a list of {@link ChatCompletionMessageParam} objects.
     * <p>
     * This method processes each {@link BaseMessage} in the input list by mapping it to a corresponding
     * {@link ChatCompletionMessageParam} based on its role. The conversion logic is encapsulated within
     * the {@link #convertMessage(BaseMessage)} method.
     * </p>
     *
     * @param messages the list of {@link BaseMessage} objects to be converted
     * @return a list of {@link ChatCompletionMessageParam} objects corresponding to the input messages
     * @throws IllegalArgumentException if any {@link BaseMessage} has an unsupported role
     * @throws NullPointerException     if the {@code messages} list or any of its elements are {@code null}
     */
    public static List<ChatCompletionMessageParam> convertMessages(List<BaseMessage> messages) {
        return messages.stream()
            .map(MessageConverter::convertMessage)
            .collect(Collectors.toList());
    }

    /**
     * Converts a single {@link BaseMessage} to a {@link ChatCompletionMessageParam} based on its role.
     * <p>
     * The method determines the role of the provided {@link BaseMessage} and maps it to the appropriate
     * {@link ChatCompletionMessageParam} subtype. Supported roles include {@code SYSTEM}, {@code USER},
     * and {@code ASSISTANT}. Roles such as {@code FUNCTION} and {@code TOOL} are acknowledged but not yet implemented.
     * </p>
     *
     * @param baseMessage the {@link BaseMessage} instance to be converted
     * @return the corresponding {@link ChatCompletionMessageParam} instance
     * @throws IllegalArgumentException if the {@code baseMessage} has an unsupported or unrecognized role
     * @throws NullPointerException     if the {@code baseMessage} is {@code null}
     */
    private static ChatCompletionMessageParam convertMessage(BaseMessage baseMessage) {
        BaseMessage.Role baseRole = BaseMessage.Role.fromString(baseMessage.getRole());
        String content = baseMessage.getContent();

        switch (baseRole) {
            case SYSTEM:
                return ChatCompletionMessageParam.ofChatCompletionSystemMessageParam(
                    ChatCompletionSystemMessageParam.builder()
                        .role(ChatCompletionSystemMessageParam.Role.SYSTEM)
                        .content(ChatCompletionSystemMessageParam.Content.ofTextContent(content))
                        .build()
                );
            case USER:
                return ChatCompletionMessageParam.ofChatCompletionUserMessageParam(
                    ChatCompletionUserMessageParam.builder()
                        .role(ChatCompletionUserMessageParam.Role.USER)
                        .content(ChatCompletionUserMessageParam.Content.ofTextContent(content))
                        .build()
                );
            case ASSISTANT:
                return ChatCompletionMessageParam.ofChatCompletionAssistantMessageParam(
                    ChatCompletionAssistantMessageParam.builder()
                        .role(ChatCompletionAssistantMessageParam.Role.ASSISTANT)
                        .content(ChatCompletionAssistantMessageParam.Content.ofTextContent(content))
                        .build()
                );
            case FUNCTION:
                // Implement FUNCTION mapping
                break;
            case TOOL:
                // Implement TOOL mapping
                break;
            default:
                throw new IllegalArgumentException("Unsupported role: " + baseRole);
        }

        // Placeholder return; actual implementation should handle all cases
        return null;
    }
}
