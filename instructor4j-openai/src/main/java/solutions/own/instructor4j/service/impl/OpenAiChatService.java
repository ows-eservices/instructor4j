package solutions.own.instructor4j.service.impl;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.core.http.StreamResponse;
import com.openai.models.ChatCompletion;
import com.openai.models.ChatCompletionChunk;
import com.openai.models.ChatCompletionCreateParams;
import solutions.own.instructor4j.service.AiChatService;

public class OpenAiChatService implements AiChatService {

    private final OpenAIClient openAiClient;

    public OpenAiChatService(String apiKey) {
        this.openAiClient = OpenAIOkHttpClient.builder()
            .apiKey(apiKey)
            .build();
    }

    @Override
    public ChatCompletion createChatCompletion(ChatCompletionCreateParams request) {
        return openAiClient.chat().completions().create(request);
    }

    @Override
    public StreamResponse<ChatCompletionChunk> createStreamChatCompletion(ChatCompletionCreateParams request) {
        return openAiClient.chat().completions().createStreaming(request);
    }
}
