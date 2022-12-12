package exmaple_classes;

import java.time.Instant;
import java.util.Objects;

public class ExampleClass {

    private String stringProperty;
    private int numberProperty;
    private Instant timeProperty;

    public ExampleClass(String stringProperty, int numberProperty, Instant timeProperty) {
        this.stringProperty = stringProperty;
        this.numberProperty = numberProperty;
        this.timeProperty = timeProperty;
    }

    @Override
    public String toString() {
        return "ExampleClass{" +
                "stringProperty='" + stringProperty + '\'' +
                ", numberProperty=" + numberProperty +
                ", timeProperty=" + timeProperty +
                '}';
    }

    // For tests purposes

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExampleClass that = (ExampleClass) o;
        return numberProperty == that.numberProperty && Objects.equals(stringProperty, that.stringProperty) && Objects.equals(timeProperty, that.timeProperty);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stringProperty, numberProperty, timeProperty);
    }
}
