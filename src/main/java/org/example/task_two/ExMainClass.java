package org.example.task_two;

import java.time.Instant;

public class ExMainClass {
    private String stringProperty;
    @Property(expectedPropertyName = "f.number")
    private int numberProperty;
    private Instant timeProperty;
    // We don't care as long as we have above types of field present
    private long id;
    // Neither about the constructor (as long as there is at least one public)


    @Override
    public String toString() {
        return "ExMainClass{" +
                "stringProperty='" + stringProperty + '\'' +
                ", numberProperty=" + numberProperty +
                ", timeProperty=" + timeProperty +
                ", id=" + id +
                '}';
    }
}
