package org.example.server;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {
    private static final Path LOG_FILE = Paths.get("server_logs.txt");
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static FileWriter fileWriter = null;

    static {
        try {

            fileWriter = new FileWriter(LOG_FILE.toString(), true);
        } catch (IOException e) {
            System.err.println("Failed to initialize logger: " + e.getMessage());
        }
    }

    public static void log(String message) {
        if (fileWriter != null) {
            String timestamp = "[" + LocalDateTime.now().format(formatter) + "] ";
            String logEntry = timestamp + message + System.lineSeparator();
            try {
                fileWriter.write(logEntry);
                fileWriter.flush();
            } catch (IOException e) {
                System.err.println("Failed to write log: " + e.getMessage());
            }
        }
    }

    public static void closeLogger() {
        if (fileWriter != null) {
            try {
                fileWriter.close();
                fileWriter = null;
            } catch (IOException e) {
                System.err.println("Failed to close logger: " + e.getMessage());
            }
        }
    }
}