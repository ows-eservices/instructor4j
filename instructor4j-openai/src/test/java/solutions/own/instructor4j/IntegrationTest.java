package solutions.own.instructor4j;

import solutions.own.instructor4j.exception.InstructorException;
import solutions.own.instructor4j.model.ConferenceData;
import solutions.own.instructor4j.service.AiChatService;
import solutions.own.instructor4j.service.impl.OpenAiChatService;
import solutions.own.instructor4j.model.User;
import solutions.own.instructor4j.config.ApiKeys;

import com.theokanning.openai.completion.chat.ChatMessage;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

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
    public void testRealAPIResponseWithComplexDataModel() {
        assertNotNull(apiKey, "OPENAI API key must be provided for integration tests.");
        assertNotEquals(ApiKeys.OPENAI_API_KEY_NOT_PROVIDED, apiKey,
            "OPENAI API key must be provided for integration tests.");

            String meetingRecord =
            "In our recent online meeting, participants from various backgrounds joined to discuss the upcoming tech conference. " +
                "The names and contact details of the participants were as follows:\n\n" +

                "- Name: John Doe, Email: johndoe@email.com, Twitter: @TechGuru44\n" +
                "- Name: Jane Smith, Email: janesmith@email.com, Twitter: @DigitalDiva88\n" +
                "- Name: Alex Johnson, Email: alexj@email.com, Twitter: @CodeMaster2023\n" +
                "- Name: Emily Clark, Email: emilyc@email.com, Twitter: @InnovateQueen\n" +
                "- Name: Ron Stewart, Email: ronstewart@email.com, Twitter: @RoboticsRon5\n" +
                "- Name: Sarah Lee, Email: sarahlee@email.com, Twitter: @AI_Aficionado\n" +
                "- Name: Mike Brown, Email: mikeb@email.com, Twitter: @FutureTechLeader\n" +
                "- Name: Lisa Green, Email: lisag@email.com, Twitter: @CyberSavvy101\n" +
                "- Name: David Wilson, Email: davidw@email.com, Twitter: @GadgetGeek77\n" +
                "- Name: Daniel Kim, Email: danielk@email.com, Twitter: @DataDrivenDude\n\n" +

                "During the meeting, we agreed on several key points. The conference will be held on March 15th, 2024, at the Grand Tech Arena " +
                "located at 4521 Innovation Drive. Dr. Emily Johnson, a renowned AI researcher, will be our keynote speaker.\n\n" +

                "The budget for the event is set at $50,000, covering venue costs, speaker fees, and promotional activities. Each participant " +
                "is expected to contribute an article to the conference blog by February 20th.\n\n" +

                "A follow-up meeting is scheduled for January 25th at 3 PM GMT to finalize the agenda and confirm the list of speakers.";

        AiChatService openAiService = new OpenAiChatService(apiKey);
        Instructor instructor = new Instructor(openAiService, 3);

        List<ChatMessage> messages = List.of(
            new ChatMessage("user", meetingRecord)
        );

        try {
            ConferenceData conferenceData = instructor.createChatCompletion(messages, "gpt-4o-mini", ConferenceData.class);

            assertNotNull(conferenceData);
            assertEquals(10, conferenceData.getConferenceParticipants().size());
            assertEquals("John Doe", conferenceData.getConferenceParticipants().get(0).getName());
            assertEquals("johndoe@email.com", conferenceData.getConferenceParticipants().get(0).getEmail());
            assertEquals("@TechGuru44", conferenceData.getConferenceParticipants().get(0).getTwitter());
            assertEquals(50000.0, conferenceData.getBudget());
            System.out.println(conferenceData);

        } catch (InstructorException e) {
            fail("InstructorException occurred: " + e.getMessage());
        }
    }
}

