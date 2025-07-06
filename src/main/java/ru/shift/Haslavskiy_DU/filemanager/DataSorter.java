package ru.shift.Haslavskiy_DU.filemanager;

import ru.shift.Haslavskiy_DU.Enums.Constants;
import ru.shift.Haslavskiy_DU.Enums.StatisticType;
import ru.shift.Haslavskiy_DU.Statistic.NumberStatistic;
import ru.shift.Haslavskiy_DU.Statistic.StringStatistic;
import ru.shift.Haslavskiy_DU.options.LaunchOptions;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class DataSorter {

    private final LaunchOptions options;
    private final ExecutorService executor;
    private final NumberStatistic integerStat = new NumberStatistic();
    private final NumberStatistic floatStat = new NumberStatistic();
    private final StringStatistic stringStat = new StringStatistic();

    public DataSorter(LaunchOptions options) {
        this.options = options;
        this.executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    public void processFiles() {
        clearOutputFilesIfRequired();
        try {
            for (String inputFile : options.getInputFiles()) {
                executor.submit(() -> {
                    try {
                        processFile(inputFile);
                    } catch (IOException e) {
                        System.err.println("Error processing file: " + inputFile + ". " + e.getMessage());
                    }
                });
            }
            executor.shutdown();
            if (!executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS)) {
                executor.shutdownNow();
            }
            summarizeStatistics();
        } catch (InterruptedException e) {
            System.err.println("Execution interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
        }
    }

    private void processFile(String inputFile) throws IOException {
        Files.lines(Paths.get(inputFile)).forEach(this::processLine);
    }

    private void processLine(String line) {
        String type;
        if (Parser.isInteger(line)) {
            type = Constants.INTEGER;
            integerStat.update(line);
        } else if (Parser.isFloat(line)) {
            type = Constants.FLOAT;
            floatStat.update(line);
        } else {
            type = Constants.STRING;
            stringStat.update(line);
        }

        try {
            writeLine(type, line);
        } catch (IOException e) {
            System.err.println("Failed to write line to file: " + e.getMessage());
        }
    }

    private synchronized void writeLine(String type, String line) throws IOException {
        String fileName = options.getOutputPath() + "/" + options.getPrefix() + type + ".txt";
        Path filePath = Paths.get(fileName);
        Path parentDir = filePath.getParent();

        if (!Files.exists(parentDir)) {
            Files.createDirectories(parentDir);
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true))) {
            writer.write(line);
            writer.newLine();
        }
    }

    private void clearOutputFilesIfRequired() {
        if (!options.isAppendMode()) {
            options.getInputFiles().forEach(inputFile -> {
                String[] fileTypes = {Constants.INTEGER, Constants.FLOAT, Constants.STRING};
                for (String type : fileTypes) {
                    String fileName = options.getOutputPath() + "/" + options.getPrefix() + type + ".txt";
                    Path filePath = Paths.get(fileName);
                    try {
                        Files.deleteIfExists(filePath);
                    } catch (IOException e) {
                        System.err.println("Failed to delete existing file: " + fileName + ". " + e.getMessage());;
                    }
                }
            });
        }
    }

    private void summarizeStatistics() {
        if (options.getStatisticType() == StatisticType.SHORT) {
            System.out.println("Short statistics:");
            System.out.println("Integers: " + integerStat.shortSummarize());
            System.out.println("Floats: " + floatStat.shortSummarize());
            System.out.println("Strings: " + stringStat.shortSummarize());
        } else if (options.getStatisticType() == StatisticType.FULL) {
            System.out.println("Full statistics:");
            System.out.println("Integers: " + integerStat.summarize());
            System.out.println("Floats: " + floatStat.summarize());
            System.out.println("Strings: " + stringStat.summarize());
        }
    }

}
