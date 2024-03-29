package com.rogermiranda1000.helper.configlib;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the annotated configuration element or type should be serialized using
 * an instance of the referenced serializer. The serializer referenced by this annotation
 * is selected regardless of whether the type of configuration element or annotated type
 * matches the type the serializer expects.
 * <p>
 * If this annotation is applied to a configuration element, and that element is an array,
 * list, set, or map a nesting level can be set to apply the serializer not to the
 * top-level type but to its elements. For maps, the serializer is applied to the values
 * and not the keys.
 * <p>
 * The following examples show how {@code nesting} can be used to apply the serializer to
 * configuration elements at different levels.
 * <p>
 * If instead the annotation is applied to a non-generic type, then, whenever that exact
 * type (i.e. not a subtype or implementation) is encountered, the referenced serializer
 * is selected.
 * <p>
 * Similarly, this annotation can be used as a meta-annotation on other {@code ElementType.TYPE}
 * annotations. In these cases, the serializer is selected whenever a type annotated with
 * the meta-annotated annotation is found. An example for this is the {@link Polymorphic}
 * annotation.
 */
@Target({
        ElementType.ANNOTATION_TYPE, // usage as meta-annotation
        ElementType.TYPE,            // usage on types
        ElementType.FIELD            // usage on configuration elements
})
@Retention(RetentionPolicy.RUNTIME)
public @interface SerializeWith {
    /**
     * Returns the type of the serializer to be used.
     *
     * @return the type of serializer to use
     */
    @SuppressWarnings("rawtypes")
    Class<? extends Serializer> serializer();

    /**
     * Returns the nesting level at which to apply the serializer.
     * <p>
     * Setting {@code nesting} to an invalid value, i.e. a negative one or one that is greater than
     * the number of levels the element actually has, results in the serializer not being selected.
     * <p>
     * If this annotation is applied to a type or another annotation, the value returned by this
     * method has no effect.
     *
     * @return the nesting level
     */
    int nesting() default 0;
}
