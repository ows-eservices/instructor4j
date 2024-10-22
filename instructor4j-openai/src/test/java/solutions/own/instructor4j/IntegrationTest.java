package solutions.own.instructor4j;

import solutions.own.instructor4j.exception.InstructorException;
import solutions.own.instructor4j.service.AiChatService;
import solutions.own.instructor4j.service.impl.OpenAiChatService;
import solutions.own.instructor4j.model.User;

import com.theokanning.openai.completion.chat.ChatMessage;
import org.junit.jupiter.api.Test;

import java.util.List;
import solutions.own.instructor4j.config.ApiKeys;

import static org.junit.jupiter.api.Assertions.*;

public class IntegrationTest {

    String apiKey = ApiKeys.OPENAI_API_KEY;

    @Test
    public void testRealAPIResponse() {
        assertNotNull(apiKey, "OPENAI API key must be provided for integration tests.");
        assertNotEquals(ApiKeys.OPENAI_API_KEY_NOT_PROVIDED, apiKey,
            "OPENAI API key must be provided for integration tests.");

        AiChatService openAiService = new OpenAiChatService(apiKey);
        Instructor instructor = new Instructor(openAiService, 3);

        List<ChatMessage> messages = List.of(
                new ChatMessage("user", "Nenad Alajbegovic is 25 years old")
        );

        try {
            User user = instructor.createChatCompletion(messages, "gpt-3.5-turbo", User.class);

            assertNotNull(user);
            assertEquals(25, user.getAge());
            assertEquals("Nenad Alajbegovic", user.getName());
            System.out.println(user);

        } catch (InstructorException e) {
            fail("InstructorException occurred: " + e.getMessage());
        }
    }

    @Test
    public void testRealAPIResponseAdvanced() {
        assertNotNull(apiKey, "OPENAI API key must be provided for integration tests.");
        assertNotEquals(ApiKeys.OPENAI_API_KEY_NOT_PROVIDED, apiKey,
            "OPENAI API key must be provided for integration tests.");

        AiChatService openAiService = new OpenAiChatService(apiKey);
        Instructor instructor = new Instructor(openAiService, 3);

        List<ChatMessage> messages = List.of(
            new ChatMessage("user", "Nenad Alajbegovic will be 27 years old next year on this same date.")
        );

        try {
            User user = instructor.createChatCompletion(messages, "gpt-3.5-turbo", User.class);

            assertNotNull(user);
            assertEquals(26, user.getAge());
            assertEquals("Nenad Alajbegovic", user.getName());
            System.out.println(user);

        } catch (InstructorException e) {
            fail("InstructorException occurred: " + e.getMessage());
        }
    }
}

