package org.example.task_two;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class TaskTwo {

    private static final Set<String> allowedTypes = new HashSet<>(Arrays.asList(
            "String", "Integer", "int", "Instant"
    ));

    /**
     * Creates and returns object of the given class with the attributes filled
     * with values from the file by the given path. The contract is, the class is
     * required to have at least one public constructor and at least one of the
     * allowed types of field {@link #allowedTypes}. It also has to have field names
     * equal to those in properties file and appropriate field types. Otherwise,
     * resulting object attributes may be null or a parsing exception is thrown.
     * If the field is annotated with{@link Property} and has non-default params,
     * annotation will be processed accordingly.
     * @param cls Class type of the object to be returned
     * @param propertiesPath path to the properties file
     * @return created object of type {@link T} with attributes filled from the file
     */

    public static <T> T loadFromProperties(Class<T> cls, Path propertiesPath) {

        // Casting (generic type erasure during compilation)
        T castedObj = cls.cast(createNewInstanceOfT(cls));
        // Getting class fields (excluding possible superclass fields)
        Field[] allFields = castedObj.getClass().getDeclaredFields();
        // Filtering fields so that we have to pass only allowed types
        Field[] fields = Arrays.stream(allFields)
                .filter(f -> allowedTypes.contains(f.getType().getSimpleName()))
                .toArray(Field[]::new);
        // Value is String as we cast them later
        Map<String, String> mapFromProperties = readPropertiesFile(propertiesPath);

        return fillObject(fields, mapFromProperties, castedObj, propertiesPath);
    }

    /**
     * Fills the object fields via reflection and returns this object.
     * @param fields array of {@link Field} with allowed types
     * @param mapFromProperties map of key-values from property file
     * @param castedObj object to fill and return
     * @param propertiesPath path in case an exception is thrown
     * @return created object of type {@link T} with attributes filled from the map
     */

    private static <T> T fillObject(Field[] fields,
                                   Map<String, String> mapFromProperties,
                                   T castedObj, Path propertiesPath) {
        for(Field field : fields) {
            String fieldName = field.getName();
            String dateTimePattern = "dd.MM.yyyy HH:mm";
            Property propertyAnnotation = field.getAnnotation(Property.class);
            // If property is present, we change the field name to then
            // look for the equal one from the properties map
            if(propertyAnnotation != null) {
                String[] annotationParams = processPropertyAnnotation(field, propertyAnnotation, propertiesPath);
                fieldName = annotationParams[0];
                dateTimePattern = annotationParams[1];
            }

            for(Map.Entry<String, String> entry : mapFromProperties.entrySet()) {
                if(fieldName.equals(entry.getKey())) {
                    field.setAccessible(true);
                    try {
                        String checkString = entry.getValue();
                        // parsing Integer or int
                        if(checkString.matches("\\d+")) {
                            parseNumber(field, castedObj, checkString, propertiesPath);
                        }
                        // String
                        else if(checkString.matches("\\w+")) {
                            parseString(field, castedObj, checkString, propertiesPath);
                        }
                        // Instant
                        else {
                            parseInstant(field, castedObj, checkString, dateTimePattern, propertiesPath);
                        }
                    } catch (IllegalAccessException e) {
                        throw new IllegalStateException("Unexpected error");
                    }
                }
            }
        }
        return castedObj;
    }

    /**
     * Processes annotation parameters.
     * <ul>
     *     <li>expectedPropertyName</li>
     *     <li>expectedDateTimeFormat</li>
     * </ul>
     * @param field field, where annotation {@link Property} is present
     * @param propertyAnnotation annotation itself
     * @param propertiesPath file path in case an exception is thrown
     * @return an array of annotation parameters
     */

    private static String[] processPropertyAnnotation(Field field,
                                                      Property propertyAnnotation,
                                                      Path propertiesPath) {
        String fieldName = propertyAnnotation.expectedPropertyName();
        // If we have prefix in annotation property, extract value after dot
        if(fieldName.contains(".")) {
            fieldName = fieldName.substring(fieldName.indexOf(".") + 1);
        }
        // expectedPropertyName not specified
        if(fieldName.equals("")) {
            fieldName = field.getName();
        }
        // Only if the field is of type Instant
        // we can process the expectedDateTimeFormat()
        String dateTimePattern = propertyAnnotation.expectedDateTimeFormat();
        // expectedDateTimeFormat specified (else we just use the standard format)
        if(!dateTimePattern.equals("dd.MM.yyyy HH:mm")) {
            // Applying  the param to field not of type Instant
            if(!field.getType().getSimpleName().equals("Instant")) {
                throw new IllegalStateException(
                        "Trying to apply param of annotation " + propertyAnnotation +
                                " not on the field of type Instant: " + field.getName() +
                                " of type " + field.getType().getSimpleName() +
                                " at path: " + propertiesPath);
            }
        }
        return new String[] {fieldName, dateTimePattern};
    }

    /**
     * Reads the property file and puts each key-value pair (separated by the "=" sign)
     * in a map.
     * @param propertiesPath file path to be read
     * @return map of key-vlue pairs from the file separated by the "=" sign
     */
    private static Map<String, String> readPropertiesFile(Path propertiesPath) {

        String s;
        Map<String, String> mapFromProperties = new HashMap<>();

        try(BufferedReader reader = new BufferedReader(
                new FileReader(String.valueOf(propertiesPath)), 4 * 1024)) {

            while ((s = reader.readLine()) != null) {
                if(!s.contains("=") || s.endsWith("=") || s.startsWith("=")) {
                    throw new IllegalStateException(
                            "Illegal format of properties file line: " + s +
                                    " at path: " + propertiesPath);
                }
                String[] keyAndValue = s.split("=");
                mapFromProperties.put(keyAndValue[0].trim(), keyAndValue[1].trim());
            }
        } catch (FileNotFoundException e) {
            throw new IllegalStateException("No file with path " + propertiesPath + " found");
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Properties file is empty
        if(mapFromProperties.isEmpty()) {
            throw new IllegalStateException("Properties file empty at path: " + propertiesPath);
        }
        //mapFromProperties.forEach((key, value) -> System.out.println(key + ":" + value));
        return mapFromProperties;
    }

    /**
     * Parses value (String) from the map to Integer or int.
     * @param field field to be filled with value
     * @param castedObj object with the field
     * @param checkString value from map to be parsed
     * @param propertiesPath path to the file in case an exception is thrown
     * @throws IllegalStateException if the parsing fails
     * - and value cannot be assigned to Integer or int
     * @throws IllegalAccessException if the field is not accessible (it is - syntax for compiler)
     */
    private static <T> void parseNumber(Field field,
                                         T castedObj,
                                         String checkString,
                                         Path propertiesPath) throws IllegalAccessException {
        if(field.getType().getSimpleName().equals("Integer") ||
                field.getType().getSimpleName().equals("int")) {
            field.set(castedObj, Integer.parseInt(checkString));
        }
        // Allowing parsing numbers only to String fields
        // as they technically can be Strings e.g. stringProperty = "459"
        else if(field.getType().getSimpleName().equals("String")) {
            field.set(castedObj, checkString);
        }
        else {
            throw new IllegalStateException(
                    "Parsing error! Cannot assign the value " + checkString +
                            " to the field " + field.getName() +
                            " at file " + propertiesPath);
        }
    }

    /**
     * Sets the field of object with value from map.
     * @param field field to be filled with value
     * @param castedObj object with the field
     * @param checkString value from map to be parsed
     * @param propertiesPath path to the file in case an exception is thrown
     * @throws IllegalStateException if the parsing fails - field is not of the type String
     * @throws IllegalAccessException if the field is not accessible (it is - syntax for compiler)
     */
    private static <T> void parseString(Field field,
                                         T castedObj,
                                         String checkString,
                                         Path propertiesPath) throws IllegalAccessException {
        if(field.getType().getSimpleName().equals("String")) {
            field.set(castedObj, checkString);
        }
        else {
            throw new IllegalStateException(
                    "Parsing error! Cannot assign the value " + checkString +
                            " to the field " + field.getName() +
                            " at file " + propertiesPath);
        }
    }

    /**
     * Parses value (String) from the map to Instant.
     * @param field field to be filled with value
     * @param castedObj object with the field
     * @param checkString value from map to be parsed
     * @param propertiesPath path to the file in case an exception is thrown
     * @throws IllegalStateException if the parsing fails - field is not of the type Instant
     * or date-time pattern is malformed
     * @throws IllegalAccessException if the field is not accessible (it is - syntax for compiler)
     */
    private static <T> void parseInstant(Field field,
                                         T castedObj,
                                         String checkString,
                                         String dateTimePattern,
                                         Path propertiesPath) throws IllegalAccessException {
        if(field.getType().getSimpleName().equals("Instant")) {
            Instant instant;
            // Catching a RunTimeException
            // If the value cannot be parsed with the specified pattern
            try {
                instant = LocalDateTime
                        .parse(checkString, DateTimeFormatter.ofPattern(dateTimePattern))
                        .atZone(ZoneId.of("UTC"))
                        .toInstant();
            } catch (DateTimeParseException e) {
                throw new IllegalStateException(
                        "String " + checkString + " cannot be parsed " +
                        "with pattern " + dateTimePattern +
                        " at file " + propertiesPath);
            }
            field.set(castedObj, instant);
        }
        else {
            throw new IllegalStateException(
                    "Parsing error! Cannot assign the value " + checkString +
                            " to the field " + field.getName() +
                            " at file " + propertiesPath + ", required type Instant");
        }
    }

    /** Since we know <STRONG>ONLY</STRONG> the Class of the T class
     * (knowing nothing about the constructors), we have to get
     * any of its constructor and create an object of that type with some
     * default values to begin with. The class is required to have a field
     * (or fields) of at least one of the required types:
     * <ul>
     *     <li>{@link Integer}</li>
     *     <li>{@link String}</li>
     *     <li>{@link Instant}</li>
     *     <li>{@code int}</li>
     * </ul>
     * But may also have fields with other data types. If only default constructor present,
     * an object with default values for each attribute is created.
     * @param cls Class type of the object to create
     * @return created object
     */

    private static <T> Object createNewInstanceOfT(Class<T> cls) {

        // No field with the necessary types to parse present
        if(Arrays.stream(cls.getDeclaredFields())
                .noneMatch(f -> allowedTypes.contains(f.getType().getSimpleName()))) {
            throw new IllegalStateException("No fields with required types!");
        }

        Constructor<?>[] constructors = cls.getConstructors();

        if(constructors.length == 0) {
            throw new IllegalStateException("No public constructors present!");
        }

        // Get the first constructor (we don't care which)
        Constructor<?> constructor = constructors[0];
        // Get its args types
        Type[] constParamTypes = constructor.getGenericParameterTypes();
        final Object[] actualParams = new Object[constParamTypes.length];
        for (int i = 0; i < constParamTypes.length; i++) {
            Type constParamType = constParamTypes[i];
            // Put the default values of the args types (primitives) in array
            checkForPrimitivesInConstructor(constParamType, actualParams, i);
        }
        Object obj;
        // Checking only exceptions that can be dealt with on our end
        try {
            // Invoking constructor
            obj = constructor.newInstance(actualParams);
        } catch (InstantiationException e) {
            throw new IllegalStateException("Provided class is abstract! Stacktrace: " + e);
        } catch (InvocationTargetException ex) {
            throw new IllegalStateException("Constructor threw an exception! Stacktrace: " + ex);
        } catch (IllegalAccessException exc) {
            throw new IllegalStateException("Constructor in not accessible! Stacktrace: " + exc);
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException("Unwrapping from array failed! Stacktrace: " + exception);
        }
        return obj;
    }

    /**
     * Checks if the argument in constructor is a primitive or not
     * @param constParamType argument to be checked
     * @param actualParams array of constructor arguments
     * @param i iteration from for loop
     */
    private static void checkForPrimitivesInConstructor(Type constParamType,
                                                        Object[] actualParams, int i) {
        // Checking for primitives in constructor. Any type of object is already null.

        // Setting primitives (nums)
        Set<Type> primitiveNumbers = new HashSet<>(Arrays.asList(
                Integer.TYPE,
                Byte.TYPE,
                Short.TYPE,
                Long.TYPE,
                Float.TYPE,
                Double.TYPE
        ));

        if(primitiveNumbers.contains(constParamType)) {
            actualParams[i] = 0;
        }
        else if(constParamType.equals(Boolean.TYPE)) {
            actualParams[i] = false;
        }
        else if(constParamType.equals(Character.TYPE)) {
            actualParams[i] = ' ';
        }
    }
}
