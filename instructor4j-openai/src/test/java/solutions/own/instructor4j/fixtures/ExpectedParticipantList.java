package solutions.own.instructor4j.fixtures;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import solutions.own.instructor4j.model.Participant;

public class ExpectedParticipantList {
    private final static String json = "{\n" +
        " \"data\": [\n" +
        "   {\n" +
        "     \"name\": \"John Doe\",\n" +
        "     \"email\": \"johndoe@email.com\",\n" +
        "     \"handle\": \"@TechGuru44\"\n" +
        "   },\n" +
        "   {\n" +
        "     \"name\": \"Jane Smith\",\n" +
        "     \"email\": \"janesmith@email.com\",\n" +
        "     \"handle\": \"@DigitalDiva88\"\n" +
        "   },\n" +
        "   {\n" +
        "     \"name\": \"Alex Johnson\",\n" +
        "     \"email\": \"alexj@email.com\",\n" +
        "     \"handle\": \"@CodeMaster2023\"\n" +
        "   },\n" +
        "   {\n" +
        "     \"name\": \"Emily Clark\",\n" +
        "     \"email\": \"emilyc@email.com\",\n" +
        "     \"handle\": \"@InnovateQueen\"\n" +
        "   },\n" +
        "   {\n" +
        "     \"name\": \"Ron Stewart\",\n" +
        "     \"email\": \"ronstewart@email.com\",\n" +
        "     \"handle\": \"@RoboticsRon5\"\n" +
        "   },\n" +
        "   {\n" +
        "     \"name\": \"Sarah Lee\",\n" +
        "     \"email\": \"sarahlee@email.com\",\n" +
        "     \"handle\": \"@AI_Aficionado\"\n" +
        "   },\n" +
        "   {\n" +
        "     \"name\": \"Mike Brown\",\n" +
        "     \"email\": \"mikeb@email.com\",\n" +
        "     \"handle\": \"@FutureTechLeader\"\n" +
        "   },\n" +
        "   {\n" +
        "     \"name\": \"Lisa Green\",\n" +
        "     \"email\": \"lisag@email.com\",\n" +
        "     \"handle\": \"@CyberSavvy101\"\n" +
        "   },\n" +
        "   {\n" +
        "     \"name\": \"David Wilson\",\n" +
        "     \"email\": \"davidw@email.com\",\n" +
        "     \"handle\": \"@GadgetGeek77\"\n" +
        "   },\n" +
        "   {\n" +
        "     \"name\": \"Daniel Kim\",\n" +
        "     \"email\": \"danielk@email.com\",\n" +
        "     \"handle\": \"@DataDrivenDude\"\n" +
        "   }\n" +
        " ]\n" +
        "}";

    public static List<Participant> getExpectedParticipants() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode root = objectMapper.readTree(json);
        JsonNode dataNode = root.get("data");
        return objectMapper.convertValue(dataNode, new TypeReference<List<Participant>>() {});
    }
}
