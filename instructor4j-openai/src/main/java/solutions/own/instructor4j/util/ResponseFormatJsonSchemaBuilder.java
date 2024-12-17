package solutions.own.instructor4j.util;

import com.openai.core.JsonValue;
import com.openai.models.ResponseFormatJsonSchema;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import solutions.own.instructor4j.annotation.Description;

public class ResponseFormatJsonSchemaBuilder {

    /**
     * Converts a given Java type into a corresponding JSON Schema type.
     *
     * <p>This method checks common Java types and maps them to standard JSON Schema
     * types.
     *
     * @param type the Java {@link Class} to convert
     * @return a {@link String} representing the equivalent JSON Schema type
     */
    public static String toJsonSchemaType(Class<?> type) {
        if (type.equals(String.class)) return "string";
        if (type.equals(int.class) || type.equals(Integer.class)
            || type.equals(long.class) || type.equals(Long.class)) return "integer";
        if (type.equals(float.class) || type.equals(Float.class)
            || type.equals(double.class) || type.equals(Double.class)) return "number";
        if (type.equals(boolean.class) || type.equals(Boolean.class)) return "boolean";
        return "string";
    }

    /**
     * Builds a JSON Schema from a given Java class by inspecting its fields.
     *
     * <p>The generated schema:
     * <ul>
     *   <li>Sets the root schema {@code type} to "object" and {@code additionalProperties} to false.</li>
     *   <li>Enumerates each field of the provided class, using the field name as the property name.</li>
     *   <li>Determines the JSON Schema type of each field based on its Java type using
     *       {@link #toJsonSchemaType(Class)}.</li>
     *   <li>Uses the {@link Description} annotation on fields (if present) to set the "description" property
     *       of each corresponding JSON Schema property. If no description is provided, a default
     *       "No description provided" is used.</li>
     *   <li>Marks all fields as required.</li>
     * </ul>
     *
     * <p>This method creates a {@link ResponseFormatJsonSchema} object that encapsulates the entire schema
     * for the given class, making it usable in contexts where a JSON schema is required to validate
     * or describe expected JSON structures.
     *
     * @param clazz the {@link Class} whose fields define the schema properties
     * @return a {@link ResponseFormatJsonSchema} instance representing the JSON Schema of the class
     */
    public static ResponseFormatJsonSchema buildSchemaFromClass(Class<?> clazz) {
        Map<String, Object> propertiesMap = new LinkedHashMap<>();
        List<String> requiredFields = new ArrayList<>();

        for (Field field : clazz.getDeclaredFields()) {
            String fieldName = field.getName();
            String fieldType = toJsonSchemaType(field.getType());

            Description desc = field.getAnnotation(Description.class);
            String fieldDescription = desc != null ? desc.value() : "No description provided";

            Map<String, Object> propertyDetails = new LinkedHashMap<>();
            propertyDetails.put("type", fieldType);
            propertyDetails.put("description", fieldDescription);

            propertiesMap.put(fieldName, propertyDetails);

            requiredFields.add(fieldName);
        }

        // Now build the schema using the generated maps
        ResponseFormatJsonSchema responseFormatJsonSchema = ResponseFormatJsonSchema.builder()
            .type(ResponseFormatJsonSchema.Type.JSON_SCHEMA)
            .jsonSchema(
                ResponseFormatJsonSchema.JsonSchema.builder()
                    .name("custom_data_schema") // arbitrary name
                    .schema(
                        ResponseFormatJsonSchema.JsonSchema.Schema.builder()
                            .putAdditionalProperty("type", JsonValue.from("object"))
                            .putAdditionalProperty("additionalProperties", JsonValue.from(false))
                            .putAdditionalProperty("properties", JsonValue.from(propertiesMap))
                            .putAdditionalProperty("required", JsonValue.from(requiredFields))
                            .build()
                    )
                    .build()
            )
            .build();

        return responseFormatJsonSchema;
    }
}
