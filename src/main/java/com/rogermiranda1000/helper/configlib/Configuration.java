package com.rogermiranda1000.helper.configlib;

import java.lang.annotation.*;

/**
 * Indicates that the annotated type is a configuration.
 * <p>
 * Configuration classes must have a no-args constructor.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface Configuration {}
