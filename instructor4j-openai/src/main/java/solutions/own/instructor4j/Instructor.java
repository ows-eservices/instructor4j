package solutions.own.instructor4j;

import static solutions.own.instructor4j.util.Utils.listOf;
import static solutions.own.instructor4j.util.Utils.removeAllEscapedQuotes;

import java.util.Collections;
import java.util.stream.Collectors;
import solutions.own.instructor4j.exception.InstructorException;
import solutions.own.instructor4j.model.BaseMessage;
import solutions.own.instructor4j.model.BaseMessage.Role;
import solutions.own.instructor4j.model.MyFunctionDefinition;
import solutions.own.instructor4j.service.AiChatService;
import solutions.own.instructor4j.util.FunctionDefinitionBuilder;
import solutions.own.instructor4j.util.MessageConverter;
import solutions.own.instructor4j.util.ResponseFormatJsonSchemaBuilder;
import solutions.own.instructor4j.util.Utils;

import com.openai.core.JsonValue;
import com.openai.core.http.StreamResponse;
import com.openai.models.ChatCompletion;
import com.openai.models.ChatCompletionChunk;
import com.openai.models.ChatCompletionChunk.Choice;
import com.openai.models.ChatCompletionCreateParams;
import com.openai.models.ChatCompletionMessageParam;
import com.openai.models.ChatCompletionMessageToolCall;
import com.openai.models.ChatCompletionTool;
import com.openai.models.ChatCompletionToolChoiceOption;
import com.openai.models.ChatCompletionToolChoiceOption.Behavior;
import com.openai.models.FunctionDefinition;
import com.openai.models.FunctionParameters;
import com.openai.models.ResponseFormatJsonSchema;

import java.util.Optional;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

/**
 * A service class for interacting with AI-based chat completions and handling retries.
 * It provides methods to send chat messages, validate responses, and retry operations
 * based on custom logic.
 */
public class Instructor {

    private final AiChatService aiChatService;
    private final int maxRetries;
    private static final Logger logger = Logger.getLogger(Instructor.class.getName());

    /**
     * Constructs an Instructor with the given AI chat service and a maximum retry count.
     *
     * @param aiChatService The AI chat client used for creating chat completions.
     * @param maxRetries The maximum number of retries allowed when trying to get a valid response.
     */
    public Instructor(AiChatService aiChatService, int maxRetries) {
        this.aiChatService = aiChatService;
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
    public <T> T createChatCompletion(List<BaseMessage> messages, String model,
        Class<T> responseModel) throws InstructorException {

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
            }

            retryCount++;
        }

        throw new InstructorException("Unable to get a valid response after " + maxRetries + " retries.");
    }

    /**
     * Creates a streaming chat completion based on the provided messages and model. This method sends a set
     * of input messages to the AI model as a streaming request, accumulates the streamed response chunks,
     * and emits them as a Flux of strings. Each emitted string is a piece of the response content returned
     * by the AI model.
     *
     * <p>The method also enforces a particular output format via a JSON schema derived from the provided
     * response model class. The model is instructed to return data that fits into this schema as a JSON array
     * named "data". Only content that matches the schema is included in the output.</p>
     *
     * <p>If an error occurs during the streaming process, the returned Flux will emit an error signal.
     * When the stream finishes, it completes the Flux.</p>
     *
     * @param baseMessages     the list of input messages that form the conversation to send to the model.
     * @param model        the name of the model to be used for the completion.
     * @param responseModel the class that defines the response format's JSON schema. The response will be
     *                      constrained to this schema.
     * @return a {@link Flux} of {@link String}, where each emitted string represents a chunk of the streamed
     *         completion response from the AI model. The Flux completes when the response stream ends, or
     *         emits an error if something goes wrong.
     * @throws RuntimeException if there is an error creating the completion parameters.
     */
    public Flux<String> createStreamChatCompletion(List<BaseMessage> baseMessages, String model,
        Class responseModel) {

        ChatCompletionCreateParams completionCreateParams =
            buildChatCompletionStreamCreateParams(baseMessages, model, responseModel);

        Sinks.Many<String> sink = Sinks.many().unicast().onBackpressureBuffer();

        try {
            assert this.aiChatService != null;
            try (StreamResponse<ChatCompletionChunk> messageStreamResponse =
                this.aiChatService.createStreamChatCompletion(completionCreateParams)) {

                List<ChatCompletionChunk> chunks = messageStreamResponse.stream().collect(
                    Collectors.toList());

                for (ChatCompletionChunk chunk : chunks) {
                    for (Choice choice : chunk.choices()) {
                        for (String content : Utils.optionalToStream(choice.delta().content())
                            .collect(Collectors.toList())) {
                            if (!content.trim().isEmpty()) {
                                sink.tryEmitNext(content);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            sink.tryEmitError(e);
        }

        sink.tryEmitComplete();

        return sink.asFlux();
    }

    /**
     * Attempts to create a chat completion using the provided messages and model.
     * Depending on the 'stream' parameter, it either performs a synchronous request
     * or a streaming request.
     *
     * @param baseMessages       the list of {@link BaseMessage} representing the conversation.
     * @param model          the model identifier to use for the chat completion.
     * @param responseModel  the class of the response model to deserialize the result into.
     * @param <T>            the type of the response model.
     * @return the deserialized response of type {@code T}.
     * @throws InstructorException if an error occurs during the chat completion process.
     * @throws IllegalArgumentException if any of the required parameters are {@code null}.
     */
    private <T> T attemptChatCompletion(List<BaseMessage> baseMessages, String model,
        Class<T> responseModel) throws InstructorException {

        validateInputs(baseMessages, model, responseModel);

        MyFunctionDefinition myFunctionDefinition = FunctionDefinitionBuilder.getFunctionDefinition(responseModel);

        if (myFunctionDefinition == null) {
            throw new InstructorException("Function definition for response model " +
                responseModel.getName() + " not found.");
        }

        ChatCompletionCreateParams completionCreateParams = buildChatCompletionCreateParams(baseMessages,
            model, myFunctionDefinition);

        try {
            assert aiChatService != null;
            ChatCompletion completion = aiChatService.createChatCompletion(completionCreateParams);
            com.openai.models.ChatCompletion.Choice choice = extractFirstChoice(completion);
            String functionCallArguments = extractToolCallArguments(choice);

            return Utils.parseJson(functionCallArguments, responseModel);

        } catch (Exception e) {
            throw new InstructorException("Error creating chat completion: " + e.getMessage(), e);
        }
    }

    /**
     * Adjusts the prompt by adding field hints to the last user message, to increase the
     * likelihood of a valid structured output.
     *
     * @param originalMessages The original list of chat messages.
     * @param responseModel The response model class.
     * @param <T> The type of the response model.
     * @return A modified list of chat messages with hints added to the last message.
     */
    private <T> List<BaseMessage> adjustPrompt(List<BaseMessage> originalMessages, Class<T> responseModel) {
        logger.info("Adjusting prompt to increase the likelihood of a valid structured output.");

        StringBuilder hint = new StringBuilder(" Please ensure the response includes the following fields: ");
        for (Field field : responseModel.getDeclaredFields()) {
            hint.append(field.getName()).append(", ");
        }
        hint.setLength(hint.length() - 2); // Remove the trailing comma and space

        BaseMessage lastMessage = originalMessages.get(originalMessages.size() - 1);
        String adjustedContent = lastMessage.getContent() + hint.toString();

        List<BaseMessage> adjustedMessages = new ArrayList<>(originalMessages);
        adjustedMessages.set(adjustedMessages.size() - 1, new BaseMessage(lastMessage.getRole(),
            adjustedContent));

        return adjustedMessages;
    }

    /**
     * Validates the input parameters for the chat completion request.
     *
     * @param baseMessages      the list of {@link BaseMessage}.
     * @param model         the model identifier.
     * @param responseModel the class of the response model.
     * @throws IllegalArgumentException if any of the parameters are {@code null}.
     */
    private void validateInputs(List<BaseMessage> baseMessages, String model, Class<?> responseModel) {
        if (baseMessages == null) {
            throw new IllegalArgumentException("Parameter 'baseMessages' must not be null.");
        }
        if (model == null || model.trim().isEmpty()) {
            throw new IllegalArgumentException("Parameter 'model' must not be null or empty.");
        }
        if (responseModel == null) {
            throw new IllegalArgumentException("Parameter 'responseModel' must not be null.");
        }
    }

    /**
     * Constructs a {@link ChatCompletionCreateParams} instance based on the provided base messages, model, and response model class.
     * <p>
     * This method performs the following operations:
     * <ol>
     *   <li>Creates a mutable copy of the input {@code baseMessages} list to allow modifications.</li>
     *   <li>Checks if the {@code baseMessages} list contains a message with the {@link BaseMessage.Role#SYSTEM} role using {@link #hasSystemRole(List)}. If absent, it adds a default system message with predefined content to the mutable list.</li>
     *   <li>Creates an immutable copy of the updated messages list to ensure thread-safety and immutability.</li>
     *   <li>Converts the immutable list of {@link BaseMessage} instances to a list of {@link ChatCompletionMessageParam} objects using {@link MessageConverter#convertMessages(List)}.</li>
     *   <li>Generates a {@link ResponseFormatJsonSchema} based on the provided {@code responseModel} class using {@link ResponseFormatJsonSchemaBuilder#buildSchemaFromClass(Class)}.</li>
     *   <li>Builds and returns a {@link ChatCompletionCreateParams} object with the specified model, response format, maximum tokens, and converted messages.</li>
     * </ol>
     * </p>
     *
     * @param baseMessages the initial list of {@link BaseMessage} objects to include in the chat completion
     *                     Must not be {@code null} and should not contain {@code null} elements.
     * @param model the identifier of the model to be used for generating the chat completion
     *              Must not be {@code null} or empty.
     * @param responseModel the {@link Class} representing the JSON schema for the response format
     *                      Must not be {@code null}.
     * @return a {@link ChatCompletionCreateParams} object configured with the provided messages, model, and response format
     * @throws NullPointerException if {@code baseMessages}, {@code model}, or {@code responseModel} is {@code null}, or if any element in {@code baseMessages} is {@code null}
     * @throws IllegalArgumentException if any {@link BaseMessage} in {@code baseMessages} has an unsupported role or if response schema generation fails
     * @see MessageConverter#convertMessages(List)
     * @see ResponseFormatJsonSchemaBuilder#buildSchemaFromClass(Class)
     */
    private ChatCompletionCreateParams buildChatCompletionStreamCreateParams(List<BaseMessage> baseMessages,
        String model, Class responseModel) {
        List<BaseMessage> mutableBaseMessages = new ArrayList<>(baseMessages);

        // Add system message if missing...
        if (!hasSystemRole(baseMessages)) {
            mutableBaseMessages.add(new BaseMessage(BaseMessage.Role.SYSTEM.getValue(),
                "You are data analyzer. Return data as a json array based on response format json schema. "
                    + "Output only data that fits into response format json schema. Always name return array 'data'"));
        }

        List<BaseMessage> finalBaseMessages = Collections.unmodifiableList(new ArrayList<>(mutableBaseMessages));

        List<ChatCompletionMessageParam> messages = MessageConverter.convertMessages(finalBaseMessages);

        ResponseFormatJsonSchema responseFormatJsonSchema = ResponseFormatJsonSchemaBuilder.buildSchemaFromClass(
            responseModel);

        return ChatCompletionCreateParams.builder()
            .responseFormat(responseFormatJsonSchema)
            .model(model)
            .maxTokens(1024)
            .messages(messages)
            .build();

    }

    /**
     * Constructs a {@link ChatCompletionCreateParams} instance based on the provided base messages, model, and function definition.
     * <p>
     * This method performs the following operations:
     * <ol>
     *   <li>Converts the list of {@link BaseMessage} objects to a list of {@link ChatCompletionMessageParam} using {@link MessageConverter#convertMessages(List)}.</li>
     *   <li>Transforms the parameters from {@link MyFunctionDefinition} into a {@code Map<String, JsonValue>}.</li>
     *   <li>Builds a {@link FunctionDefinition} with the provided name, description, and parameters, setting {@code additionalProperties} to {@code false} and marking the schema as non-strict.</li>
     *   <li>Creates a {@link ChatCompletionTool} of type {@link ChatCompletionTool.Type#FUNCTION} with the constructed {@link FunctionDefinition}.</li>
     *   <li>Assembles the final {@link ChatCompletionCreateParams} using the builder pattern, incorporating tool choices, tools, model identifier, maximum tokens, and messages.</li>
     * </ol>
     * </p>
     *
     * @param baseMessages          the initial list of {@link BaseMessage} objects to include in the chat completion
     *                              <p>
     *                              Must not be {@code null} and should not contain {@code null} elements.
     *                              </p>
     * @param model                 the identifier of the model to be used for generating the chat completion
     *                              <p>
     *                              Must not be {@code null} or empty.
     *                              </p>
     * @param myFunctionDefinition  the {@link MyFunctionDefinition} instance containing function details
     *                              <p>
     *                              Must not be {@code null}. Should provide a valid name, description, and parameters.
     *                              </p>
     * @return a {@link ChatCompletionCreateParams} object configured with the provided messages, model, and function definition
     * @throws NullPointerException     if {@code baseMessages}, {@code model}, or {@code myFunctionDefinition} is {@code null},
     *                                  or if any element within {@code baseMessages} is {@code null}
     * @throws IllegalArgumentException if {@code model} is empty, if {@code myFunctionDefinition} contains invalid data,
     *                                  or if any parameter conversion fails
     * @see ChatCompletionCreateParams
     * @see BaseMessage
     * @see MessageConverter#convertMessages(List)
     * @see MyFunctionDefinition
     */
    private ChatCompletionCreateParams buildChatCompletionCreateParams(List<BaseMessage> baseMessages,
        String model, MyFunctionDefinition myFunctionDefinition) {

        List<ChatCompletionMessageParam> messages = MessageConverter.convertMessages(baseMessages);

        java.util.Map<String, JsonValue> parameters = Utils.convertMap(myFunctionDefinition.getParameters());

        return ChatCompletionCreateParams.builder()
            .toolChoice(
                ChatCompletionToolChoiceOption.ofBehavior(
                    Behavior.AUTO
                )
            )
            .tools(
                listOf(
                    ChatCompletionTool.builder()
                        .function(
                            com.openai.models.FunctionDefinition.builder()
                                .name(myFunctionDefinition.getName())
                                .description(myFunctionDefinition.getDescription())
                                .parameters(FunctionParameters.builder()
                                    .putAllAdditionalProperties(parameters)
                                    .putAdditionalProperty("additionalProperties", JsonValue.from(false))
                                    .putAdditionalProperty("required", JsonValue.from(parameters.get("required")))
                                    .build())
                                .strict(false)
                                .build()
                        )
                        .type(ChatCompletionTool.Type.FUNCTION)
                        .build()
                )
            )
            .model(model)
            .maxTokens(1024)
            .messages(messages)
            .build();
    }

    /**
     * Extracts the first {@link com.openai.models.ChatCompletion.Choice} from the {@link ChatCompletion}.
     *
     * @param completion the {@link ChatCompletion} received from the API.
     * @return the first {@link com.openai.models.ChatCompletion.Choice}.
     * @throws InstructorException if no choices are available.
     */
    private com.openai.models.ChatCompletion.Choice extractFirstChoice(ChatCompletion completion)
        throws InstructorException {
        List<com.openai.models.ChatCompletion.Choice> choices = completion.choices();
        if (choices == null || choices.isEmpty()) {
            throw new InstructorException("No choices returned from chat completion.");
        }
        return choices.get(0);
    }

    /**
     * Extracts and cleans the function call arguments from a {@link com.openai.models.ChatCompletion.Choice}.
     *
     * @param choice the {@link com.openai.models.ChatCompletion.Choice} to extract from.
     * @return the cleaned function call arguments.
     * @throws InstructorException if function call arguments are missing.
     */
    private String extractToolCallArguments(com.openai.models.ChatCompletion.Choice choice) throws InstructorException {
        Optional<List<ChatCompletionMessageToolCall>> toolCalls = choice.message().toolCalls();
        if (!toolCalls.isPresent() || toolCalls.get().get(0) == null) {
            throw new InstructorException("Tool call arguments are missing in the chat completion response.");
        }
        return removeAllEscapedQuotes(toolCalls.get().get(0).function().arguments());
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
     * Checks whether the provided list of {@link BaseMessage} contains at least one message with the {@code SYSTEM} role.
     * <p>
     * This method iterates through the list of {@link BaseMessage} objects and determines if any message
     * has a role that matches {@link BaseMessage.Role#SYSTEM}. The comparison is case-insensitive,
     * ensuring that roles like "SYSTEM", "system", or "System" are all considered equivalent.
     * </p>
     *
     * @param messages the list of {@link BaseMessage} objects to be checked
     * @return {@code true} if at least one message in the list has the {@code SYSTEM} role; {@code false} otherwise
     * @throws NullPointerException if the {@code messages} list or any of its elements are {@code null}
     */
    private boolean hasSystemRole(List<BaseMessage> messages) {
        return messages.stream()
            .anyMatch(message -> Role.SYSTEM.getValue().equalsIgnoreCase(message.getRole()));
    }
}