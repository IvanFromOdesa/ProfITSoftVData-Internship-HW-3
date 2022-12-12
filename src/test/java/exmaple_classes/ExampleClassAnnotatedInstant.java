package exmaple_classes;

import org.example.task_two.Property;

import java.time.Instant;
import java.util.Objects;

public class ExampleClassAnnotatedInstant {
    private String stringProperty;
    private int numberProperty;
    @Property(expectedDateTimeFormat = "dd.MM.yyyy HH:mm:ss")
    private Instant timeProperty;

    public ExampleClassAnnotatedInstant(String stringProperty, int numberProperty, Instant timeProperty) {
        this.stringProperty = stringProperty;
        this.numberProperty = numberProperty;
        this.timeProperty = timeProperty;
    }

    @Override
    public String toString() {
        return "ExampleClassAnnotatedInstant{" +
                "stringProperty='" + stringProperty + '\'' +
                ", numberProperty=" + numberProperty +
                ", timeProperty=" + timeProperty +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExampleClassAnnotatedInstant that = (ExampleClassAnnotatedInstant) o;
        return numberProperty == that.numberProperty && Objects.equals(stringProperty, that.stringProperty) && Objects.equals(timeProperty, that.timeProperty);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stringProperty, numberProperty, timeProperty);
    }
}
