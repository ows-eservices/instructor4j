package solutions.own.instructor4j;

import com.openai.models.ChatCompletion;
import com.openai.models.ChatCompletionMessage;
import com.openai.models.ChatCompletionMessageToolCall;
import java.util.Arrays;
import java.util.Collections;
import solutions.own.instructor4j.exception.InstructorException;
import solutions.own.instructor4j.model.BaseMessage;
import solutions.own.instructor4j.model.User;
import solutions.own.instructor4j.service.AiChatService;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static solutions.own.instructor4j.util.Utils.listOf;

public class InstructorTest {

    @Test
    public void testSuccessfulResponse() throws Exception {

        AiChatService mockService = mock(AiChatService.class);

        String functionArguments = "{\"age\":30,\"name\":\"Nenad Alajbegovic\"}";

        ChatCompletionMessage chatCompletionMessage = ChatCompletionMessage.builder().toolCalls(
            listOf(
                ChatCompletionMessageToolCall.builder()
                    .id("id")
                    .function(
                        ChatCompletionMessageToolCall.Function.builder()
                            .arguments(functionArguments)
                            .name("User")
                            .build()
                    )
                    .type(ChatCompletionMessageToolCall.Type.FUNCTION)
                    .build()
            )
        ).build();

        com.openai.models.ChatCompletion.Choice choice =
            com.openai.models.ChatCompletion.Choice.builder().message(chatCompletionMessage).build();

        ChatCompletion mockResult = ChatCompletion.builder().choices(
            Collections.unmodifiableList(Arrays.asList(choice))).build();

        when(mockService.createChatCompletion(any())).thenReturn(mockResult);

        Instructor instructor = new Instructor(mockService, 3);

        List<BaseMessage> messages = Collections.unmodifiableList(Arrays.asList(
            new BaseMessage(BaseMessage.Role.USER.getValue(), "Nenad Alajbegovic is 30 years old")
        ));

        User user = instructor.createChatCompletion(messages, "gpt-4o-mini", User.class);

        assertNotNull(user);
        assertEquals(30, user.getAge());
        assertEquals("Nenad Alajbegovic", user.getName());
    }

    @Test
    public void testValidationFailureAndRetry() throws Exception {

        AiChatService mockService = mock(AiChatService.class);

         //First response missing 'name' field
        String functionArguments1 = "{\"age\":30}";

        // Second response with all required fields
        String functionArguments2 = "{\"age\":30,\"name\":\"Nenad Alajbegovic\"}";

        ChatCompletionMessage chatCompletionMessage1 = ChatCompletionMessage.builder().toolCalls(
            listOf(
                ChatCompletionMessageToolCall.builder()
                    .id("id1")
                    .function(
                        ChatCompletionMessageToolCall.Function.builder()
                            .arguments(functionArguments1)
                            .name("User")
                            .build()
                    )
                    .type(ChatCompletionMessageToolCall.Type.FUNCTION)
                    .build()
            )
        ).build();

        com.openai.models.ChatCompletion.Choice choice1 =
            com.openai.models.ChatCompletion.Choice.builder().message(chatCompletionMessage1).build();

        ChatCompletion mockResult1 = ChatCompletion.builder().choices(Collections.unmodifiableList(
            Arrays.asList(choice1))).build();


        ChatCompletionMessage chatCompletionMessage2 = ChatCompletionMessage.builder().toolCalls(
            listOf(
                ChatCompletionMessageToolCall.builder()
                    .id("id2")
                    .function(
                        ChatCompletionMessageToolCall.Function.builder()
                            .arguments(functionArguments2)
                            .name("User")
                            .build()
                    )
                    .type(ChatCompletionMessageToolCall.Type.FUNCTION)
                    .build()
            )
        ).build();

        com.openai.models.ChatCompletion.Choice choice2 =
            com.openai.models.ChatCompletion.Choice.builder().message(chatCompletionMessage2).build();

        ChatCompletion mockResult2 = ChatCompletion.builder().choices(Collections.unmodifiableList(
            Arrays.asList(choice2))).build();

        when(mockService.createChatCompletion(any()))
                .thenReturn(mockResult1)
                .thenReturn(mockResult2);

        Instructor instructor = new Instructor(mockService, 3);

        List<BaseMessage> messages = Collections.unmodifiableList(Arrays.asList(
            new BaseMessage(BaseMessage.Role.USER.getValue(), "Nenad Alajbegovic is 30 years old")
        ));

        User user = instructor.createChatCompletion(messages, "gpt-4o-mini", User.class);

        verify(mockService, times(2)).createChatCompletion(any());

        assertNotNull(user);
        assertEquals(30, user.getAge());
        assertEquals("Nenad Alajbegovic", user.getName());
    }

    @Test
    public void testMaxRetriesExceeded() throws Exception {

        AiChatService mockService = mock(AiChatService.class);

        String functionArguments = "{\"age\":30}";

        ChatCompletionMessage chatCompletionMessage = ChatCompletionMessage.builder().toolCalls(
            listOf(
                ChatCompletionMessageToolCall.builder()
                    .id("id")
                    .function(
                        ChatCompletionMessageToolCall.Function.builder()
                            .arguments(functionArguments)
                            .name("User")
                            .build()
                    )
                    .type(ChatCompletionMessageToolCall.Type.FUNCTION)
                    .build()
            )
        ).build();

        com.openai.models.ChatCompletion.Choice choice =
            com.openai.models.ChatCompletion.Choice.builder().message(chatCompletionMessage).build();

        ChatCompletion mockResult = ChatCompletion.builder().choices(Collections.unmodifiableList(
            Arrays.asList(choice))).build();

        when(mockService.createChatCompletion(any())).thenReturn(mockResult);

        Instructor instructor = new Instructor(mockService, 3);

        List<BaseMessage> messages = Collections.unmodifiableList(Arrays.asList(
            new BaseMessage(BaseMessage.Role.USER.getValue(), "Nenad Alajbegovic is 30 years old")
        ));

        assertThrows(InstructorException.class, () -> {
            instructor.createChatCompletion(messages, "gpt-4o-mini", User.class);
        });

        verify(mockService, times(3)).createChatCompletion(any());
    }
}
