package org.example;

import org.example.task_one.TaskOne;
import org.example.task_two.ExMainClass;
import org.example.task_two.TaskTwo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

public class Main {

    public static void main(String[] args) throws IOException {
        taskOne();
        //taskTwo();
    }

    private static void taskOne() throws IOException {

        final String format = "json";

        final List<File> fileList = new ArrayList<>();
        // Get all the JSON files from the moving_violations directory
        try (Stream<Path> paths = Files.walk(Paths.get(
                "src/main/resources/moving_violations/" + format))) {
            paths.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith("." + format))
                    .forEach(path -> fileList.add(new File(String.valueOf(path))));
        }
        Date start = new Date();
        File output = TaskOne.overallViolationsStats(fileList);
        Date end = new Date();
        System.out.println("Time taken: " + (end.getTime() - start.getTime()) + "ms"); // time taken
        // Check the output file
        /*try(BufferedReader reader = new BufferedReader(new FileReader(output))) {
            reader.lines().forEach(System.out::println);
        }*/
    }

    private static void taskTwo() {
        ExMainClass resultObj = TaskTwo.loadFromProperties(ExMainClass.class, Path.of("src/main/resources/app.properties"));
        System.out.println(resultObj);
    }
}