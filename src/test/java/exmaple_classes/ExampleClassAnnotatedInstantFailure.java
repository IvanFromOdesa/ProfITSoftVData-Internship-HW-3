package exmaple_classes;

import org.example.task_two.Property;

import java.time.Instant;

public class ExampleClassAnnotatedInstantFailure {
    private String stringProperty;
    @Property(expectedDateTimeFormat = "dd.MM.yyyy HH:mm::ss")
    private int numberProperty;
    private Instant timeProperty;
}
