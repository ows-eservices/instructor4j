package solutions.own.instructor4j;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Assertions;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import solutions.own.instructor4j.exception.InstructorException;
import solutions.own.instructor4j.fixtures.ExpectedParticipantList;
import solutions.own.instructor4j.model.BaseMessage;
import solutions.own.instructor4j.model.Participant;
import solutions.own.instructor4j.service.AiChatService;
import solutions.own.instructor4j.service.impl.OpenAiChatService;
import solutions.own.instructor4j.model.User;

import org.junit.jupiter.api.Test;

import java.util.List;
import solutions.own.instructor4j.config.ApiKeys;
import solutions.own.instructor4j.util.Utils;

import static org.junit.jupiter.api.Assertions.*;

public class IntegrationTest {

    String apiKey = ApiKeys.OPENAI_API_KEY;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testRealAPIResponse() {
        assertNotNull(apiKey, "OPENAI API key must be provided for integration tests.");
        assertNotEquals(ApiKeys.OPENAI_API_KEY_NOT_PROVIDED, apiKey,
            "OPENAI API key must be provided for integration tests.");

        AiChatService openAiService = new OpenAiChatService(apiKey);
        Instructor instructor = new Instructor(openAiService, 3);

        List<BaseMessage> messages = Collections.unmodifiableList(Arrays.asList(
            new BaseMessage(BaseMessage.Role.USER.getValue(), "Nenad Alajbegovic is 25 years old")
        ));

        try {
            User user = instructor.createChatCompletion(messages, "gpt-4o-mini", User.class);

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

        List<BaseMessage> messages = Collections.unmodifiableList(Arrays.asList(
            new BaseMessage(BaseMessage.Role.USER.getValue(), "Nenad Alajbegovic will be 27 years old next year on this same date.")
        ));

        try {
            User user = instructor.createChatCompletion(messages, "gpt-4o-mini", User.class);

            assertNotNull(user);
            assertEquals(26, user.getAge());
            assertEquals("Nenad Alajbegovic", user.getName());
            System.out.println(user);

        } catch (InstructorException e) {
            fail("InstructorException occurred: " + e.getMessage());
        }
    }

    @Test
    public void testRealAPIResponseStreamed() throws JsonProcessingException {
        assertNotNull(apiKey, "OPENAI API key must be provided for integration tests.");
        assertNotEquals(ApiKeys.OPENAI_API_KEY_NOT_PROVIDED, apiKey,
            "OPENAI API key must be provided for integration tests.");

        String meetingRecord = "In our recent online meeting, participants from various backgrounds joined to discuss the upcoming tech conference. " +
            "The names and contact details of the participants were as follows:\n" +
            "\n" +
            "- Name: John Doe, Email: johndoe@email.com, Twitter: @TechGuru44\n" +
            "- Name: Jane Smith, Email: janesmith@email.com, Twitter: @DigitalDiva88\n" +
            "- Name: Alex Johnson, Email: alexj@email.com, Twitter: @CodeMaster2023\n" +
            "- Name: Emily Clark, Email: emilyc@email.com, Twitter: @InnovateQueen\n" +
            "- Name: Ron Stewart, Email: ronstewart@email.com, Twitter: @RoboticsRon5\n" +
            "- Name: Sarah Lee, Email: sarahlee@email.com, Twitter: @AI_Aficionado\n" +
            "- Name: Mike Brown, Email: mikeb@email.com, Twitter: @FutureTechLeader\n" +
            "- Name: Lisa Green, Email: lisag@email.com, Twitter: @CyberSavvy101\n" +
            "- Name: David Wilson, Email: davidw@email.com, Twitter: @GadgetGeek77\n" +
            "- Name: Daniel Kim, Email: danielk@email.com, Twitter: @DataDrivenDude";

        AiChatService openAiService = new OpenAiChatService(apiKey);
        Instructor instructor = new Instructor(openAiService, 3);

        StringBuilder fullResponseReceived = new StringBuilder();

        List<Participant> expectedParticipants = ExpectedParticipantList.getExpectedParticipants();

        List<BaseMessage> messages = Collections.unmodifiableList(Arrays.asList(
            new BaseMessage(BaseMessage.Role.USER.getValue(), meetingRecord)
        ));

        Flux<String> extractionStream;

        extractionStream = instructor.createStreamChatCompletion(
            messages,
            "gpt-4o-mini",
            Participant.class
        );

        extractionStream
            .publishOn(Schedulers.boundedElastic())
            .doOnNext(extraction -> {
                if (extraction != null) {
                    fullResponseReceived.append(extraction);

                    // Let us assure that json received always have balanced quotes, curly braces, and square brackets
                    String consistentJson = Utils.ensureJsonClosures(fullResponseReceived.toString());
                    assertTrue(Utils.isJsonValidOnClosures(consistentJson), "The json should always have valid closures");

                    // Let's print the received objects as they arrive during the streaming process.
                    // This will give you a clear idea of the data volume being transmitted, and help you understand
                    // how we might efficiently send and display it in the front end.
                    JsonNode rootNode;
                    try {
                        rootNode = objectMapper.readTree(consistentJson);
                        JsonNode dataNode = rootNode.get("data");
                        List<Participant> participants = objectMapper.convertValue(dataNode, new TypeReference<List<Participant>>() {});
                        if (participants != null) {
                            for (Participant p : participants) {
                                System.out.println(
                                    "    PARTICIPANT DATA RECEIVED: " + p.getName() + ", " + p.getEmail() + ", "
                                        + p.getHandle());
                            }
                            System.out.println("\n");
                        }
                    } catch (JsonProcessingException e) {
                        ; // perfectly fine
                    }
                }
            })
            .doOnError(error -> {
                Assertions.fail("Flux emitted an unexpected error: " + error.getMessage());
            })
            .doOnComplete(() -> {
                try {
                    List<Participant> completedParticipants = Utils.getEntities(
                        fullResponseReceived.toString().replaceAll("\\n", ""),
                        Participant.class,
                        "data");

                    assertEquals(completedParticipants, expectedParticipants, "Participant list not as expected");

                } catch (JsonProcessingException e) {
                    Assertions.fail("Not able to interpretate data: " + e.getMessage());
                }
            })
            .blockLast();
    }
}

