package org.example.task_two;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Property {
    String expectedPropertyName() default "";
    // Default format : 23.11.2002 12:30
    String expectedDateTimeFormat() default "dd.MM.yyyy HH:mm";
}
