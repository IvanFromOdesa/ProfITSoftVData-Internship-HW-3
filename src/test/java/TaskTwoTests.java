import exmaple_classes.*;
import org.example.task_two.TaskTwo;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.*;

public class TaskTwoTests {

    @Test
    void shouldReturnObjectT_whenCalledWithSuccess() {
        // given
        Class<ExampleClass> cls = ExampleClass.class;
        Path path = Path.of("src/test/resources/app.properties");

        // when
        ExampleClass result = TaskTwo.loadFromProperties(cls, path);

        // then
        LocalDateTime dateTime = LocalDateTime
                .parse("29.11.2022 18:30", DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
        ExampleClass expected = new ExampleClass(
                "98kio98",
                9,
                dateTime.toInstant(ZoneOffset.UTC));

        System.out.println("Actual: " + result);
        // Equals and hashcode overriden
        assertEquals(expected, result);
    }

    @Test
    void shouldThrowException_whenFileNotFound() {
        // given
        Class<ExampleClass> cls = ExampleClass.class;
        Path path = Path.of("src/test/resources/appThatDoesNotExist.properties");

        // when
        IllegalStateException e = assertThrows(
                IllegalStateException.class,
                () -> TaskTwo.loadFromProperties(cls, path),
                "Expected loadFromProperties() to throw, but it didn't"
        );

        // then
        assertTrue(e.getMessage().contains("No file with path "));
    }

    @Test
    void shouldThrowException_whenNoPublicConstructorsPresent() {
        // given
        Class<ExampleClassNoPublicConstr> cls = ExampleClassNoPublicConstr.class;
        Path path = Path.of("src/test/resources/app.properties");

        // when
        IllegalStateException e = assertThrows(
                IllegalStateException.class,
                () -> TaskTwo.loadFromProperties(cls, path),
                "Expected loadFromProperties() to throw, but it didn't"
        );

        // then
        assertTrue(e.getMessage().contains("No public constructors "));
    }

    @Test
    void shouldThrowException_whenNoAllowedFieldTypesPresent() {
        // given
        Class<ExampleClassNoAllowedFTypes> cls = ExampleClassNoAllowedFTypes.class;
        Path path = Path.of("src/test/resources/app.properties");

        // when
        IllegalStateException e = assertThrows(
                IllegalStateException.class,
                () -> TaskTwo.loadFromProperties(cls, path),
                "Expected loadFromProperties() to throw, but it didn't"
        );

        // then
        assertTrue(e.getMessage().contains("No fields with required "));
    }

    @Test
    void shouldThrowException_whenClassIsAbstract() {
        // given
        Class<ExampleClassAbstract> cls = ExampleClassAbstract.class;
        Path path = Path.of("src/test/resources/app.properties");

        // when
        IllegalStateException e = assertThrows(
                IllegalStateException.class,
                () -> TaskTwo.loadFromProperties(cls, path),
                "Expected loadFromProperties() to throw, but it didn't"
        );

        // then
        assertTrue(e.getMessage().contains("Provided class is abstract"));
    }

    @Test
    void shouldThrowException_whenIllegalFormatInPropsFile() {
        // given
        Class<ExampleClass> cls = ExampleClass.class;
        Path path = Path.of("src/test/resources/appIllegalFormat.properties");

        // when
        IllegalStateException e = assertThrows(
                IllegalStateException.class,
                () -> TaskTwo.loadFromProperties(cls, path),
                "Expected loadFromProperties() to throw, but it didn't"
        );

        // then
        assertTrue(e.getMessage().contains("Illegal format of properties"));
    }

    @Test
    void shouldThrowException_whenPropsFileEmpty() {
        // given
        Class<ExampleClass> cls = ExampleClass.class;
        Path path = Path.of("src/test/resources/appEmpty.properties");

        // when
        IllegalStateException e = assertThrows(
                IllegalStateException.class,
                () -> TaskTwo.loadFromProperties(cls, path),
                "Expected loadFromProperties() to throw, but it didn't"
        );

        // then
        assertTrue(e.getMessage().contains("Properties file empty "));
    }

    @Test
    void shouldThrowException_whenClassFieldParsingFails() {
        // given
        Class<ExampleClassFieldNameSameTypeDiff> cls = ExampleClassFieldNameSameTypeDiff.class;
        Path path = Path.of("src/test/resources/app.properties");

        // when
        IllegalStateException e = assertThrows(
                IllegalStateException.class,
                () -> TaskTwo.loadFromProperties(cls, path),
                "Expected loadFromProperties() to throw, but it didn't"
        );

        // then
        assertTrue(e.getMessage().contains("Parsing error! Cannot assign the value "));
    }

    @Test
    void shouldThrowException_whenInstantParsingFails() {
        // given
        Class<ExampleClassParseFailureInstant> cls = ExampleClassParseFailureInstant.class;
        Path path = Path.of("src/test/resources/app.properties");

        // when
        IllegalStateException e = assertThrows(
                IllegalStateException.class,
                () -> TaskTwo.loadFromProperties(cls, path),
                "Expected loadFromProperties() to throw, but it didn't"
        );

        // then
        assertTrue(e.getMessage().contains("required type Instant"));
    }

    @Test
    void shouldThrowException_whenPropertyFileWithMalformedDateTime() {
        // given
        Class<ExampleClass> cls = ExampleClass.class;
        Path path = Path.of("src/test/resources/appMalformedDateTimeFormat.properties");

        // when
        IllegalStateException e = assertThrows(
                IllegalStateException.class,
                () -> TaskTwo.loadFromProperties(cls, path),
                "Expected loadFromProperties() to throw, but it didn't"
        );

        // We did not specify date-time pattern with annotation
        // then
        assertTrue(e.getMessage().contains("cannot be parsed with pattern"));
    }

    @Test
    void shouldReturnObject_whenAnnotationCorrectParamName_andNoParsingException() {
        // given
        Class<ExampleClassAnnotated> cls = ExampleClassAnnotated.class;
        Path path = Path.of("src/test/resources/appForAnnotation.properties");

        // when
        ExampleClassAnnotated result = TaskTwo.loadFromProperties(cls, path);

        // then
        LocalDateTime dateTime = LocalDateTime
                .parse("29.11.2022 18:30", DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
        ExampleClassAnnotated expected = new ExampleClassAnnotated(
                "98kio98",
                9,
                dateTime.toInstant(ZoneOffset.UTC));

        System.out.println("Actual: " + result);
        // Equals and hashcode overriden
        assertEquals(expected, result);
    }

    @Test
    void shouldThrowException_whenAnnotationUseExpectedInstantParamNotOnInstant() {
        // given
        Class<ExampleClassAnnotatedInstantFailure> cls = ExampleClassAnnotatedInstantFailure.class;
        Path path = Path.of("src/test/resources/appDateTimeFormatChange.properties");

        // when
        IllegalStateException e = assertThrows(
                IllegalStateException.class,
                () -> TaskTwo.loadFromProperties(cls, path),
                "Expected loadFromProperties() to throw, but it didn't"
        );

        // then
        assertTrue(e.getMessage().contains("not on the field of type Instant:"));
    }

    @Test
    void shouldReturnObject_whenAnnotationChangeDateTimeFormat_andNoParseException() {
        // given
        Class<ExampleClassAnnotatedInstant> cls = ExampleClassAnnotatedInstant.class;
        Path path = Path.of("src/test/resources/appDateTimeFormatChange.properties");

        // when
        ExampleClassAnnotatedInstant result = TaskTwo.loadFromProperties(cls, path);

        // then
        LocalDateTime dateTime = LocalDateTime
                .parse("29.11.2022 18:30:34", DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss"));
        ExampleClassAnnotatedInstant expected = new ExampleClassAnnotatedInstant(
                "98kio98",
                9,
                dateTime.toInstant(ZoneOffset.UTC));

        System.out.println("Actual: " + result);
        // Equals and hashcode overriden
        assertEquals(expected, result);
    }
}
