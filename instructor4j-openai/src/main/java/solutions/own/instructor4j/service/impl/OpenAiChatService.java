package solutions.own.instructor4j.service.impl;

import solutions.own.instructor4j.service.AiChatService;
import com.theokanning.openai.service.OpenAiService;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;

/**
 * Implementation of {@link AiChatService} that integrates with OpenAI's API.
 * This service uses the OpenAiService to create chat completions based on requests.
 */
public class OpenAiChatService implements AiChatService {

    private final OpenAiService openAiService;

    /**
     * Constructs an instance of {@link OpenAiChatService} with the provided API token.
     *
     * @param token The OpenAI API token used to authenticate requests.
     */
    public OpenAiChatService(String token) {
        this.openAiService = new OpenAiService(token);
    }

    /**
     * Creates a chat completion by sending the provided request to the OpenAI API.
     *
     * @param request The chat completion request containing the input and parameters.
     * @return The result of the chat completion, including the generated response from OpenAI.
     */
    @Override
    public ChatCompletionResult createChatCompletion(ChatCompletionRequest request) {
        return openAiService.createChatCompletion(request);
    }
}