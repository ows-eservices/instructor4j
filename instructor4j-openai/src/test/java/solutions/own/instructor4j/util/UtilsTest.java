package solutions.own.instructor4j.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

public class UtilsTest {

    @Test
    public void testGetJsonType_String() {
        assertEquals("string", Utils.getJsonType(String.class));
    }

    @Test
    public void testGetJsonType_Integer() {
        assertEquals("integer", Utils.getJsonType(Integer.class));
        assertEquals("integer", Utils.getJsonType(int.class));
    }

    @Test
    public void testGetJsonType_Double() {
        assertEquals("number", Utils.getJsonType(Double.class));
        assertEquals("number", Utils.getJsonType(double.class));
    }

    @Test
    public void testGetJsonType_Float() {
        assertEquals("number", Utils.getJsonType(Float.class));
        assertEquals("number", Utils.getJsonType(float.class));
    }

    @Test
    public void testGetJsonType_Boolean() {
        assertEquals("boolean", Utils.getJsonType(Boolean.class));
        assertEquals("boolean", Utils.getJsonType(boolean.class));
    }

    @Test
    public void testGetJsonType_List() {
        assertEquals("array", Utils.getJsonType(List.class));
        assertEquals("array", Utils.getJsonType(java.util.ArrayList.class)); // Checking with a specific List implementation
    }

    @Test
    public void testGetJsonType_Map() {
        assertEquals("object", Utils.getJsonType(Map.class));
        assertEquals("object", Utils.getJsonType(java.util.HashMap.class)); // Checking with a specific Map implementation
    }

    @Test
    public void testGetJsonType_Object() {
        assertEquals("object", Utils.getJsonType(Object.class));
    }

    @Test
    public void testGetJsonType_CustomClass() {
        class CustomClass {}
        assertEquals("object", Utils.getJsonType(CustomClass.class));
    }

    @Test
    public void testGetOrDefault_NullValue_ReturnsDefault() {
        String result = Utils.getOrDefault(null, "default");
        assertEquals("default", result, "Should return the default value when the input value is null.");
    }

    @Test
    public void testGetOrDefault_NonNullValue_ReturnsValue() {
        String result = Utils.getOrDefault("actualValue", "default");
        assertEquals("actualValue", result, "Should return the actual value when it is not null.");
    }

    @Test
    public void testGetOrDefault_NullDefault_ReturnsNull() {
        String result = Utils.getOrDefault("value", null);
        assertEquals("value", result, "Should return the value when the default is null.");
    }

    @Test
    public void testGetOrDefault_BothNulls_ReturnsNull() {
        String result = Utils.getOrDefault(null, null);
        assertEquals(null, result, "Should return null when both value and default are null.");
    }

    @Test
    public void testGetOrDefault_IntegerValues() {
        Integer result = Utils.getOrDefault(null, 10);
        assertEquals(10, result, "Should return the default integer when the input value is null.");

        result = Utils.getOrDefault(5, 10);
        assertEquals(5, result, "Should return the actual integer value when it is not null.");
    }
}