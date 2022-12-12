package exmaple_classes;

import org.example.task_two.Property;

import java.time.Instant;
import java.util.Objects;

public class ExampleClassAnnotated {

    // If the specified property name not found in file, they remain null or 0
    @Property(expectedPropertyName = "stringN")
    private String stringProperty;
    @Property(expectedPropertyName = "number")
    private Integer numberProperty;
    private Instant timeProperty;

    public ExampleClassAnnotated(String stringProperty,
                                 int numberProperty,
                                 Instant timeProperty) {
        this.stringProperty = stringProperty;
        this.numberProperty = numberProperty;
        this.timeProperty = timeProperty;
    }

    @Override
    public String toString() {
        return "ExampleClassAnnotated{" +
                "stringProperty='" + stringProperty + '\'' +
                ", numberProperty=" + numberProperty +
                ", timeProperty=" + timeProperty +
                '}';
    }

    // For test purposes

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExampleClassAnnotated that = (ExampleClassAnnotated) o;
        return Objects.equals(stringProperty, that.stringProperty) && Objects.equals(numberProperty, that.numberProperty) && Objects.equals(timeProperty, that.timeProperty);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stringProperty, numberProperty, timeProperty);
    }
}
