package exmaple_classes;

import java.time.Instant;

public class ExampleClassFieldNameSameTypeDiff {

    // We can parse  e.g. 89 to "89"
    private String numberProperty;
    // We can't parse "Stringvalue" to Integer
    private Integer stringProperty;
    // Same goes for Instant
    /**
     * see {@link ExampleClassParseFailureInstant}
     */
    private Instant timeProperty;
}
