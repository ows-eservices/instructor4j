package solutions.own.instructor4j.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation for providing descriptions to fields in model classes.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Description {

    /**
     * The description associated with the annotated element.
     *
     * @return A string containing the description.
     */
    String value();
}
