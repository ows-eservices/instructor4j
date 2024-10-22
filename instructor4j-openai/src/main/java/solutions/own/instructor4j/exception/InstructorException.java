package solutions.own.instructor4j.exception;

/**
 * Custom exception class for handling errors related to the Instructor service.
 */
public class InstructorException extends Exception {

    /**
     * Constructs a new InstructorException with the specified detail message.
     *
     * @param message The detail message, which is saved for later retrieval by the {@link Throwable#getMessage()} method.
     */
    public InstructorException(String message) {
        super(message);
    }

    /**
     * Constructs a new InstructorException with the specified detail message and cause.
     *
     * @param message The detail message, which is saved for later retrieval by the {@link Throwable#getMessage()} method.
     * @param cause The cause of the exception, which is saved for later retrieval by the {@link Throwable#getCause()} method.
     *              A {@code null} value indicates that the cause is nonexistent or unknown.
     */
    public InstructorException(String message, Throwable cause) {
        super(message, cause);
    }
}