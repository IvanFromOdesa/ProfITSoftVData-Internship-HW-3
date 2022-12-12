package org.example.task_one;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import org.example.task_one.json_map_converter.Output;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class TaskOne {

    // For the files of size 12 mb each, 22 files, 500 thousand str in each file
    // 4 and 8 work pretty much with the same speed (equal conditions)
    // 6 cores, 12 threads, 8GB DDR4 3200 RAM
    // 8 threads = to read: +- 1100
    // 4 threads = to read: +-1100
    // 2 threads = to read: +-1400
    // single-thread = to read: +-1800
    // If we increase the size and number of files, 8 threads do outperform 4
    private static final ExecutorService cpuBound = Executors.newFixedThreadPool(8);

    /**
     Since File objects in Java are not the actual files meaning they do not contain the data but
     only directory info, input is not memory-consuming.
     @param inputFiles list of previously found JSON files
     @return Output XML file
     */
    public static File overallViolationsStats(List<File> inputFiles) {
        return readFromJSONWriteToXML(inputFiles);
    }

    /**
     * Read data from JSON files, return an output XML file.
     * First, we map Java objects from each input JSON and put them all in a list.
     * Second, we calculate for each {@link ViolationType} total fine sum of each
     * {@link TrafficViolation}. We get a Map of {@link ViolationType}
     * and total fine sum for it. Finally, we write the map into the XML using
     * custom serializer.
     * @param inputFiles list of JSON files
     * @return output XML file
     */
    private static File readFromJSONWriteToXML(List<File> inputFiles) {
        File output = new File("output_2.xml");

        List<TrafficViolation> violationList = getListOfTrfViolations(inputFiles);

        Map<ViolationType, BigDecimal> statsMap = getSortedMapOfVTypeAndTotalFineSum(violationList);

        writeMapToXml(statsMap, output);
        return output;
    }

    /**
     * Get the list of TrafficViolation.
     *
     * @param inputFiles list of inputFiles to be mapped
     * @return List of TrafficViolation
     */
    private static List<TrafficViolation> getListOfTrfViolations(List<File> inputFiles) {
        /*
        List of results from the loop - using static method allOf()
        we wait till all the Futures will complete, but they complete
        in parallel in contradiction to if we were to put the join()
        immediately in the for loop
        */
        List<CompletableFuture<List<TrafficViolation>>> completableFutureList = new ArrayList<>();
        // Synchronized as several threads modify it at the same time
        List<TrafficViolation> violationList =  Collections.synchronizedList(new ArrayList<>());
        // nanoTime() is the most precise way to count time
        long start = System.nanoTime();

        // Applying CompletableFuture for multiple threads to read files
        for (File inputFile : inputFiles) {
            CompletableFuture<List<TrafficViolation>> completableFuture = CompletableFuture
                    .supplyAsync(() -> inputFile, cpuBound)
                    .thenApply(file -> deserializeFromJson(file, violationList));
            completableFutureList.add(completableFuture);

            // One (main) thread reads the files (to test the performance difference)
            // Comment out the CompletableFuture parts
            // deserializeFromJson(inputFile, violationList);

        }
        cpuBound.shutdown();
        CompletableFuture.allOf(completableFutureList.toArray(new CompletableFuture[0]))
                // Avoiding throwing an exception in the join() call
                .exceptionally(ex -> null)
                .join();

        long end = System.nanoTime();
        long passed = (end - start) / 1000000;
        System.out.println("Time taken to read: " + passed + "ms");

        return violationList;
    }

    /**
     * Parses JSON objects into the Java objects.
     * @param file json file to deserialize
     * @param violationList list of TrafficViolation to be filled with the Java objects
     * @return list of TrafficViolation
     */
    private static List<TrafficViolation> deserializeFromJson(File file,
                                                       List<TrafficViolation> violationList) {
        // Immutability to ensure thread-safety
        final ObjectMapper mapper = new ObjectMapper();
        System.out.println(Thread.currentThread().getName() + " running");
        try (JsonParser jsonParser = mapper.getFactory().createParser(file)) {
            if (jsonParser.nextToken() != JsonToken.START_ARRAY) {
                throw new IllegalStateException("Expected content to be an array");
            }
            while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
                violationList.add(mapper.readValue(jsonParser, TrafficViolation.class));
            }
            System.out.println(Thread.currentThread().getName() + " finished");

        } catch (IOException e) {
            e.printStackTrace();
        }
        return violationList;
    }

    /**
     * Get the sorted Map of ViolationType and total fine amount.
     * @param violationList list of TrafficViolation
     * @return Map sorted descending by total sum of fine amount of each ViolationType
     */
    private static Map<ViolationType, BigDecimal> getSortedMapOfVTypeAndTotalFineSum(
            List<TrafficViolation> violationList) {

        // Get the Map of ViolationType and total fine amount
        Map<ViolationType, BigDecimal> statsMap = violationList.stream()
                .collect(Collectors.groupingBy(TrafficViolation::getType,
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                TrafficViolation::getFine_amount,
                                BigDecimal::add)));

        // Sort by fine amount
        return statsMap.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (e1, e2) -> e1, LinkedHashMap::new));

    }

    /**
     * Writes the given map of ViolationType and values for each of them into the output xml.
     * @param resultMap map of ViolationType and values for each of them
     * @param output output xml  file
     */
    private static void writeMapToXml(Map<ViolationType, BigDecimal> resultMap, File output) {

        Output out = new Output();
        out.setEntry(resultMap);

        // Write to the output file
        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
        try (FileWriter fileWriter = new FileWriter(output);
             PrintWriter printWriter = new PrintWriter(fileWriter)){
            printWriter.print(xmlMapper.writerWithDefaultPrettyPrinter().writeValueAsString(out));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
