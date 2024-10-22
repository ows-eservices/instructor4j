package solutions.own.instructor4j.model;

import java.util.Map;

/**
 * Represents a function definition with a name, description, and parameters.
 * This class uses the builder pattern for construction.
 */
public class FunctionDefinition {
    private String name;
    private String description;
    private Map<String, Object> parameters;

    /**
     * Private constructor to enforce the use of the {@link Builder} for creating instances.
     *
     * @param builder The builder used to construct the FunctionDefinition instance.
     */
    private FunctionDefinition(Builder builder) {
        this.name = builder.name;
        this.description = builder.description;
        this.parameters = builder.parameters;
    }

    /**
     * Returns the name of the function.
     *
     * @return The function name.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the description of the function.
     *
     * @return The function description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the parameters of the function.
     *
     * @return A map of parameters for the function.
     */
    public Map<String, Object> getParameters() {
        return parameters;
    }

    /**
     * Creates a new instance of the {@link Builder} for constructing a {@link FunctionDefinition}.
     *
     * @return A new {@link Builder} instance.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder class for constructing {@link FunctionDefinition} instances.
     */
    public static class Builder {
        private String name;
        private String description;
        private Map<String, Object> parameters;

        /**
         * Sets the name for the function.
         *
         * @param name The name of the function.
         * @return The current {@link Builder} instance.
         */
        public Builder name(String name) {
            this.name = name;
            return this;
        }

        /**
         * Sets the description for the function.
         *
         * @param description The description of the function.
         * @return The current {@link Builder} instance.
         */
        public Builder description(String description) {
            this.description = description;
            return this;
        }

        /**
         * Sets the parameters for the function.
         *
         * @param parameters A map of parameters for the function.
         * @return The current {@link Builder} instance.
         */
        public Builder parameters(Map<String, Object> parameters) {
            this.parameters = parameters;
            return this;
        }

        /**
         * Builds and returns a new {@link FunctionDefinition} instance.
         *
         * @return A new {@link FunctionDefinition} instance.
         */
        public FunctionDefinition build() {
            return new FunctionDefinition(this);
        }
    }
}