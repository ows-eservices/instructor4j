package solutions.own.instructor4j;

import static solutions.own.instructor4j.util.Utils.getJsonType;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import solutions.own.instructor4j.annotation.Description;
import solutions.own.instructor4j.exception.InstructorException;
import solutions.own.instructor4j.model.FunctionDefinition;
import solutions.own.instructor4j.service.AiChatService;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatCompletionChoice;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * A service class for interacting with AI-based chat completions and handling retries.
 * It provides methods to send chat messages, validate responses, and retry operations
 * based on custom logic.
 */
public class Instructor {

    private final AiChatService aiChatService;
    private final ObjectMapper objectMapper;
    private final int maxRetries;
    private static final Logger logger = Logger.getLogger(Instructor.class.getName());

    /**
     * Constructs an Instructor with the given AI chat service and a maximum retry count.
     *
     * @param aiChatService The AI chat service used for creating chat completions.
     * @param maxRetries The maximum number of retries allowed when trying to get a valid response.
     */
    public Instructor(AiChatService aiChatService, int maxRetries) {
        this.aiChatService = aiChatService;
        this.objectMapper = new ObjectMapper();
        this.maxRetries = maxRetries;
    }

    /**
     * Creates a chat completion by sending a list of chat messages and processing the result.
     * The method retries based on the maxRetries setting if validation fails.
     *
     * @param messages The list of chat messages to send.
     * @param model The AI model to use for the chat completion.
     * @param responseModel The class type expected in the response.
     * @param <T> The type of the response model.
     * @return A structured response of type T based on the chat completion result.
     * @throws InstructorException If the completion fails after the maximum number of retries.
     */
    public <T> T createChatCompletion(List<ChatMessage> messages, String model, Class<T> responseModel) throws InstructorException {
        int retryCount = 0;

        while (retryCount < maxRetries) {
            logger.info("Attempt #" + (retryCount + 1) + " to get structured response.");

            try {
                T response = attemptChatCompletion(messages, model, responseModel);
                if (validateResponse(response, responseModel)) {
                    return response;
                } else {
                    logger.warning("Validation failed. Retrying with adjusted prompt.");
                    messages = adjustPrompt(messages, responseModel);
                }
            } catch (Exception e) {
                logger.severe("Error occurred: " + e.getMessage());
                if (retryCount == maxRetries - 1) {
                    throw new InstructorException("Maximum retries reached. Unable to validate response.", e);
                }
                logger.warning("Retrying with adjusted prompt.");
                messages = adjustPrompt(messages, responseModel);
            }

            retryCount++;
        }

        throw new InstructorException("Unable to get a valid response after " + maxRetries + " retries.");
    }

    /**
     * Attempts to create a chat completion by calling the AI service and mapping the result to the response model.
     *
     * @param messages The list of chat messages to send.
     * @param model The AI model to use for the chat completion.
     * @param responseModel The class type expected in the response.
     * @param <T> The type of the response model.
     * @return The structured response of type T.
     * @throws InstructorException If there is an error processing the result or mapping it to the response model.
     */
    private <T> T attemptChatCompletion(List<ChatMessage> messages, String model, Class<T> responseModel) throws InstructorException {
        FunctionDefinition functionDefinition = getFunctionDefinition(responseModel);

        ChatCompletionRequest request = ChatCompletionRequest.builder()
            .model(model)
            .messages(messages)
            .functions(Arrays.asList(functionDefinition))
            .build();

        ChatCompletionResult completion = aiChatService.createChatCompletion(request);
        ChatCompletionChoice choice = completion.getChoices().get(0);

        String functionCall = String.valueOf(choice.getMessage().getFunctionCall().getArguments());

        try {
            return objectMapper.readValue(functionCall, responseModel);
        } catch (JsonProcessingException e) {
            throw new InstructorException(e.getMessage());
        }
    }

    /**
     * Validates the response by ensuring that all fields in the response model are non-null.
     *
     * @param response The response to validate.
     * @param modelClass The class of the response model.
     * @param <T> The type of the response model.
     * @return True if the response is valid, false otherwise.
     */
    private <T> boolean validateResponse(T response, Class<T> modelClass) {
        try {
            for (Field field : modelClass.getDeclaredFields()) {
                field.setAccessible(true);
                Object value = field.get(response);
                if (value == null) {
                    logger.warning("Field '" + field.getName() + "' is null or missing.");
                    return false;
                }
            }
            return true;
        } catch (IllegalAccessException e) {
            logger.severe("Validation error: " + e.getMessage());
            return false;
        }
    }

    /**
     * Adjusts the prompt by adding field hints to the last user message, to increase the likelihood of a valid structured output.
     *
     * @param originalMessages The original list of chat messages.
     * @param responseModel The response model class.
     * @param <T> The type of the response model.
     * @return A modified list of chat messages with hints added to the last message.
     */
    private <T> List<ChatMessage> adjustPrompt(List<ChatMessage> originalMessages, Class<T> responseModel) {
        logger.info("Adjusting prompt to increase the likelihood of a valid structured output.");

        StringBuilder hint = new StringBuilder(" Please ensure the response includes the following fields: ");
        for (Field field : responseModel.getDeclaredFields()) {
            hint.append(field.getName()).append(", ");
        }
        hint.setLength(hint.length() - 2); // Remove the trailing comma and space

        ChatMessage lastMessage = originalMessages.get(originalMessages.size() - 1);
        String adjustedContent = lastMessage.getContent() + hint.toString();

        List<ChatMessage> adjustedMessages = new ArrayList<>(originalMessages);
        adjustedMessages.set(adjustedMessages.size() - 1, new ChatMessage(lastMessage.getRole(), adjustedContent));

        return adjustedMessages;
    }

    /**
     * Generates a FunctionDefinition object based on the fields of the response model.
     *
     * @param responseModel The response model class.
     * @param <T> The type of the response model.
     * @return A FunctionDefinition object for the response model.
     */
    private <T> FunctionDefinition getFunctionDefinition(Class<T> responseModel) {
        Map<String, Object> properties = new HashMap<>();

        for (Field field : responseModel.getDeclaredFields()) {
            Map<String, Object> fieldProps = new HashMap<>();
            fieldProps.put("type", getJsonType(field.getType()));

            if (field.isAnnotationPresent(Description.class)) {
                Description description = field.getAnnotation(Description.class);
                fieldProps.put("description", description.value());
            } else {
                fieldProps.put("description", "The " + field.getName());
            }

            properties.put(field.getName(), fieldProps);
        }

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("type", "object");
        parameters.put("properties", properties);
        parameters.put("required", properties.keySet());

        return FunctionDefinition.builder()
            .name(responseModel.getSimpleName())
            .description("Generate structured data based on the given class")
            .parameters(parameters)
            .build();
    }
}