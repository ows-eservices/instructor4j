package solutions.own.instructor4j;

import solutions.own.instructor4j.exception.InstructorException;
import solutions.own.instructor4j.model.User;
import solutions.own.instructor4j.service.AiChatService;
import solutions.own.instructor4j.service.impl.OpenAiChatService;

import com.theokanning.openai.completion.chat.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class InstructorTest {

    ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testSuccessfulResponse() throws Exception {

        AiChatService mockService = mock(OpenAiChatService.class);
        ObjectMapper objectMapper = new ObjectMapper();

        String functionArguments = "{\"age\":30,\"name\":\"Nenad Alajbegovic\"}";

        ChatMessage functionCallMessage = new ChatMessage("assistant");
        functionCallMessage.setFunctionCall(new ChatFunctionCall("User", objectMapper.readTree(functionArguments)));

        ChatCompletionChoice choice = new ChatCompletionChoice();
        choice.setMessage(functionCallMessage);

        ChatCompletionResult mockResult = new ChatCompletionResult();
        mockResult.setChoices(List.of(choice));

        when(mockService.createChatCompletion(any())).thenReturn(mockResult);

        Instructor instructor = new Instructor(mockService, 3);

        List<ChatMessage> messages = List.of(
                new ChatMessage("user", "Nenad Alajbegovic is 30 years old")
        );

        User user = instructor.createChatCompletion(messages, "gpt-4o-mini", User.class);

        assertNotNull(user);
        assertEquals(30, user.getAge());
        assertEquals("Nenad Alajbegovic", user.getName());
    }

    @Test
    public void testValidationFailureAndRetry() throws Exception {

        AiChatService mockService = mock(OpenAiChatService.class);

        // First response missing 'name' field
        String functionArguments1 = "{\"age\":30}";

        // Second response with all required fields
        String functionArguments2 = "{\"age\":30,\"name\":\"Nenad Alajbegovic\"}";

        ChatMessage functionCallMessage1 = new ChatMessage("assistant");
        functionCallMessage1.setFunctionCall(new ChatFunctionCall("User", objectMapper.readTree(functionArguments1)));

        ChatCompletionChoice choice1 = new ChatCompletionChoice();
        choice1.setMessage(functionCallMessage1);

        ChatCompletionResult mockResult1 = new ChatCompletionResult();
        mockResult1.setChoices(List.of(choice1));

        ChatMessage functionCallMessage2 = new ChatMessage("assistant");
        functionCallMessage2.setFunctionCall(new ChatFunctionCall("User", objectMapper.readTree(functionArguments2)));

        ChatCompletionChoice choice2 = new ChatCompletionChoice();
        choice2.setMessage(functionCallMessage2);

        ChatCompletionResult mockResult2 = new ChatCompletionResult();
        mockResult2.setChoices(List.of(choice2));

        when(mockService.createChatCompletion(any()))
                .thenReturn(mockResult1)
                .thenReturn(mockResult2);

        Instructor instructor = new Instructor(mockService, 3);

        List<ChatMessage> messages = List.of(
                new ChatMessage("user", "Nenad Alajbegovic is 30 years old")
        );

        User user = instructor.createChatCompletion(messages, "gpt-4o-mini", User.class);

        verify(mockService, times(2)).createChatCompletion(any());

        assertNotNull(user);
        assertEquals(30, user.getAge());
        assertEquals("Nenad Alajbegovic", user.getName());
    }

    @Test
    public void testMaxRetriesExceeded() throws Exception {

        AiChatService mockService = mock(OpenAiChatService.class);

        String functionArguments = "{\"age\":30}";

        ChatMessage functionCallMessage = new ChatMessage("assistant");
        functionCallMessage.setFunctionCall(new ChatFunctionCall("User", objectMapper.readTree(functionArguments)));

        ChatCompletionChoice choice = new ChatCompletionChoice();
        choice.setMessage(functionCallMessage);

        ChatCompletionResult mockResult = new ChatCompletionResult();
        mockResult.setChoices(List.of(choice));

        when(mockService.createChatCompletion(any())).thenReturn(mockResult);

        Instructor instructor = new Instructor(mockService, 3);

        List<ChatMessage> messages = List.of(
                new ChatMessage("user", "Nenad Alajbegovic is 30 years old")
        );

        assertThrows(InstructorException.class, () -> {
            instructor.createChatCompletion(messages, "gpt-4o-mini", User.class);
        });

        verify(mockService, times(3)).createChatCompletion(any());
    }
}
