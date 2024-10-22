package solutions.own.instructor4j.service;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;

/**
 * This interface defines a service for interacting with an AI chat model.
 */
public interface AiChatService {

    /**
     * Creates a chat completion result based on the provided request.
     *
     * @param request The chat completion request containing the input messages and parameters.
     * @return The result of the chat completion, including the generated response from the AI model.
     */
    ChatCompletionResult createChatCompletion(ChatCompletionRequest request);
}
