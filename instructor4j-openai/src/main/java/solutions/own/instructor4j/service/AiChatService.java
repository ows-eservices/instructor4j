package solutions.own.instructor4j.service;

import com.openai.client.OpenAIClient;
import com.openai.core.http.StreamResponse;
import com.openai.models.ChatCompletion;
import com.openai.models.ChatCompletionChunk;
import com.openai.models.ChatCompletionCreateParams;

/**
 * The {@code AiChatService} interface defines the contract for interacting with the GenAI Chat API.
 * <p>
 * This service provides methods to create both synchronous and streaming chat completions. It abstracts
 * the underlying communication with the OpenAI API, offering a simplified and consistent interface
 * for generating chat responses based on specified parameters.
 * </p>
 *
 * <p><b>Responsibilities:</b></p>
 * <ul>
 *   <li>Generating complete chat completions based on provided parameters.</li>
 *   <li>Initiating streaming chat completions to receive partial responses in real-time.</li>
 *   <li>Handling configuration and communication details with the OpenAI API.</li>
 * </ul>
 */
public interface AiChatService {

    /**
     * Creates a chat completion by sending the specified {@link ChatCompletionCreateParams} to the OpenAI API.
     *
     * <p>This method delegates the creation of a chat completion to the underlying {@link OpenAIClient}.
     * It sends the provided {@link ChatCompletionCreateParams}, which contains the necessary parameters
     * for generating the chat completion, and returns the resulting {@link ChatCompletion}.
     * </p>
     *
     * <p>Use this method when you require a synchronous response from the OpenAI API, where the
     * entire completion is generated and returned in a single response. This is suitable for scenarios
     * where streaming or partial responses are not necessary.</p>
     *
     * @param request the {@link ChatCompletionCreateParams} containing the parameters for generating the chat completion.
     *                Must not be {@code null} and should include all required fields such as the model, messages,
     *                and any additional configuration options.
     *
     * @return a {@link ChatCompletion} representing the outcome of the chat completion request.
     *         This includes the generated messages, usage statistics, and any other relevant information
     *         returned by the OpenAI API.
     *
     * @throws IllegalArgumentException if the {@code request} is {@code null} or contains invalid parameters.
     *
     * @see ChatCompletionCreateParams
     * @see ChatCompletion
     */
    ChatCompletion createChatCompletion(ChatCompletionCreateParams request);

    /**
     * Initiates a streaming chat completion request to the OpenAI API and returns a reactive stream of response chunks.
     *
     * <p>This method sends the specified {@link ChatCompletionCreateParams} to the OpenAI API and returns a
     * {@link StreamResponse StreamResponse&lt;ChatCompletionChunk&gt;} that emits {@link ChatCompletionChunk} instances
     * as they are received. This allows for handling streaming responses in real-time, enabling applications
     * to process partial results as they become available.</p>
     *
     * @param request the {@link ChatCompletionCreateParams} containing the parameters for generating the chat completion.
     *                Must not be {@code null} and should contain valid configuration for the desired completion.
     *
     * @return a {@link StreamResponse StreamResponse&lt;ChatCompletionChunk&gt;} that emits {@link ChatCompletionChunk}
     *         instances as they are received from the OpenAI API. Subscribers can process each chunk in real-time.
     *
     * @throws IllegalArgumentException if the {@code request} is {@code null} or contains invalid parameters.
     *
     * @see ChatCompletionCreateParams
     * @see ChatCompletionChunk
     * @see StreamResponse
     */
    StreamResponse<ChatCompletionChunk> createStreamChatCompletion(ChatCompletionCreateParams request);
}
