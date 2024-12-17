package solutions.own.instructor4j.util;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import solutions.own.instructor4j.annotation.Description;
import solutions.own.instructor4j.model.MyFunctionDefinition;

/**
 * A utility builder class responsible for generating {@link MyFunctionDefinition} instances based on
 * provided response model classes. It supports processing of nested objects and arrays of class instances
 * to accurately construct the corresponding JSON schema.
 * <p>
 * This class leverages Java Reflection to introspect the fields of the response model class, applying
 * appropriate JSON schema types and constraints based on field annotations such as {@link NotNull},
 * {@link Size}, {@link Min}, {@link Max}, {@link Pattern}, and {@link Email}.
 * </p>
 *
 * <p><b>Example Usage:</b></p>
 * <pre>{@code
 * // Assuming a response model class User with appropriate annotations
 * MyFunctionDefinition functionDef = FunctionDefinitionBuilder.getFunctionDefinition(User.class);
 * }</pre>
 *
 * <p><b>Thread Safety:</b> This class is thread-safe as it does not maintain any mutable shared state.</p>
 */
public class FunctionDefinitionBuilder {

    private static final Logger logger = Logger.getLogger(FunctionDefinitionBuilder.class.getName());

    /**
     * Generates a MyFunctionDefinition based on the provided response model class.
     * Supports nested objects and arrays of class instances.
     *
     * @param responseModel The class representing the response model.
     * @param <T> The type of the response model.
     * @return A MyFunctionDefinition instance representing the JSON schema of the response model.
     */
    public static <T> MyFunctionDefinition getFunctionDefinition(Class<T> responseModel) {
        Map<String, Object> properties = new HashMap<>();
        Set<String> requiredFields = new HashSet<>();
        Set<Class<?>> processedClasses = new HashSet<>();

        processClass(responseModel, properties, requiredFields, processedClasses);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("type", "object");
        parameters.put("properties", properties);
        parameters.put("required", new ArrayList<>(requiredFields));

        return MyFunctionDefinition.builder()
            .name(responseModel.getSimpleName())
            .description("Generate structured data based on the given class")
            .parameters(parameters)
            .build();
    }

    /**
     * Recursively processes a class to build its JSON schema properties and required fields.
     *
     * @param clazz           The class to process.
     * @param properties      The map to populate with property schemas.
     * @param requiredFields  The set to populate with required field names.
     * @param processedClasses A set to track processed classes and prevent infinite recursion.
     */
    private static void processClass(Class<?> clazz, Map<String, Object> properties, Set<String> requiredFields, Set<Class<?>> processedClasses) {
        if (processedClasses.contains(clazz)) {
            logger.warning("Already processed class: " + clazz.getName() + ". Skipping to prevent recursion.");
            return;
        }
        processedClasses.add(clazz);

        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true); // In case of private fields

            String fieldName = field.getName();
            Map<String, Object> fieldSchema = new HashMap<>();

            Class<?> fieldType = field.getType();
            String jsonType = getJsonType(fieldType);

            if ("array".equals(jsonType)) {
                Map<String, Object> itemsSchema = new HashMap<>();
                Class<?> itemType = getCollectionItemType(field);
                if (itemType == null) {
                    itemsSchema.put("type", "string");
                } else {
                    String itemJsonType = getJsonType(itemType);
                    if ("object".equals(itemJsonType)) {
                        Map<String, Object> nestedProperties = new HashMap<>();
                        Set<String> nestedRequired = new HashSet<>();
                        processClass(itemType, nestedProperties, nestedRequired, processedClasses);
                        itemsSchema.put("type", "object");
                        itemsSchema.put("properties", nestedProperties);
                        itemsSchema.put("required", new ArrayList<>(nestedRequired));
                    } else {
                        itemsSchema.put("type", itemJsonType);
                        addFieldConstraints(itemsSchema, field, itemType);
                    }
                }

                fieldSchema.put("type", "array");
                fieldSchema.put("items", itemsSchema);
            } else if ("object".equals(jsonType)) {
                Map<String, Object> nestedProperties = new HashMap<>();
                Set<String> nestedRequired = new HashSet<>();
                processClass(fieldType, nestedProperties, nestedRequired, processedClasses);

                fieldSchema.put("type", "object");
                fieldSchema.put("properties", nestedProperties);
                fieldSchema.put("required", new ArrayList<>(nestedRequired));
            } else {
                fieldSchema.put("type", jsonType);
                addFieldConstraints(fieldSchema, field, fieldType);
            }

            if (field.isAnnotationPresent(Description.class)) {
                Description description = field.getAnnotation(Description.class);
                fieldSchema.put("description", description.value());
            } else {
                fieldSchema.put("description", "The " + fieldName);
            }

            properties.put(fieldName, fieldSchema);

            // Determine if the field is required (e.g., annotated with @NotNull or similar)
            if (isFieldRequired(field)) {
                requiredFields.add(fieldName);
            }
        }
    }

    /**
     * Determines if a field is required based on its annotations.
     * This example checks for @NotNull, @NotEmpty, and @NotBlank.
     *
     * @param field The field to check.
     * @return True if the field is required; false otherwise.
     */
    private static boolean isFieldRequired(Field field) {
        return field.isAnnotationPresent(NotNull.class) ||
            field.isAnnotationPresent(NotEmpty.class) ||
            field.isAnnotationPresent(NotBlank.class);
    }

    /**
     * Adds additional constraints to the field schema based on annotations.
     *
     * @param fieldSchema The field schema map to populate.
     * @param field       The field being processed.
     * @param fieldType   The type of the field.
     */
    private static void addFieldConstraints(Map<String, Object> fieldSchema, Field field, Class<?> fieldType) {
        // Handle @Min and @Max for numeric fields
        if (field.isAnnotationPresent(Min.class)) {
            Min min = field.getAnnotation(Min.class);
            fieldSchema.put("minimum", min.value());
        }

        if (field.isAnnotationPresent(Max.class)) {
            Max max = field.getAnnotation(Max.class);
            fieldSchema.put("maximum", max.value());
        }

        // Handle @Pattern for string fields
        if (field.isAnnotationPresent(Pattern.class)) {
            Pattern pattern = field.getAnnotation(Pattern.class);
            fieldSchema.put("pattern", pattern.regexp());
        }

        // Handle @Size for strings and collections
        if (field.isAnnotationPresent(Size.class)) {
            Size size = field.getAnnotation(Size.class);
            if (fieldType.equals(String.class)) {
                fieldSchema.put("minLength", size.min());
                fieldSchema.put("maxLength", size.max());
            } else if (Collection.class.isAssignableFrom(fieldType) || fieldType.isArray()) {
                fieldSchema.put("minItems", size.min());
                fieldSchema.put("maxItems", size.max());
            }
        }

        // Handle @Email for string fields
        if (field.isAnnotationPresent(Email.class)) {
            fieldSchema.put("format", "email");
        }
    }

    /**
     * Determines the JSON type based on the Java class.
     *
     * @param type The Java class.
     * @return The corresponding JSON type as a string.
     */
    private static String getJsonType(Class<?> type) {
        if (type.equals(String.class)) {
            return "string";
        } else if (type.equals(int.class) || type.equals(Integer.class) ||
            type.equals(long.class) || type.equals(Long.class) ||
            type.equals(short.class) || type.equals(Short.class) ||
            type.equals(byte.class) || type.equals(Byte.class)) {
            return "integer";
        } else if (type.equals(float.class) || type.equals(Float.class) ||
            type.equals(double.class) || type.equals(Double.class)) {
            return "number";
        } else if (type.equals(boolean.class) || type.equals(Boolean.class)) {
            return "boolean";
        } else if (type.isArray() || Collection.class.isAssignableFrom(type)) {
            return "array";
        } else {
            return "object";
        }
    }

    /**
     * Retrieves the item type of a collection or array field.
     *
     * @param field The field representing the collection or array.
     * @return The Class of the collection's items, or null if it cannot be determined.
     */
    private static Class<?> getCollectionItemType(Field field) {
        if (field.getType().isArray()) {
            return field.getType().getComponentType();
        } else if (Collection.class.isAssignableFrom(field.getType())) {
            Type genericType = field.getGenericType();
            if (genericType instanceof ParameterizedType) {
                Type[] typeArgs = ((ParameterizedType) genericType).getActualTypeArguments();
                if (typeArgs.length > 0) {
                    Type itemType = typeArgs[0];
                    if (itemType instanceof Class<?>) {
                        return (Class<?>) itemType;
                    } else if (itemType instanceof ParameterizedType) {
                        return (Class<?>) ((ParameterizedType) itemType).getRawType();
                    }
                }
            }
        }
        return null;
    }
}
