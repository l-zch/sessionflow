package com.sessionflow.runner;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles command-line argument parsing and system property configuration.
 */
public class CommandLineProcessor {

    /**
     * Parses command-line arguments and applies them to system properties.
     * 
     * @param args the command-line arguments
     */
    public static void processArguments(String[] args) {
        Map<String, String> cliArgs = parseArguments(args);
        applyCliArguments(cliArgs);
    }

    /**
     * Parses command-line arguments into a map.
     * 
     * @param args the command-line arguments
     * @return a map of parsed arguments
     */
    private static Map<String, String> parseArguments(String[] args) {
        Map<String, String> cliArgs = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "-v":
                case "--verbose":
                    cliArgs.put("verbose", "true");
                    break;
                case "-d":
                case "--debug":
                    cliArgs.put("debug", "true");
                    break;
                case "--port":
                    if (i + 1 < args.length && args[i + 1].matches("\\d+")) {
                        cliArgs.put("port", args[i + 1]);
                        i++; // Skip next argument
                    } else {
                        System.err.println("Error: --port flag requires a numeric argument.");
                        System.exit(1);
                    }
                    break;
            }
        }
        return cliArgs;
    }

    /**
     * Applies parsed command-line arguments to system properties.
     * 
     * @param cliArgs the parsed command-line arguments
     */
    private static void applyCliArguments(Map<String, String> cliArgs) {
        if (cliArgs.containsKey("port")) {
            System.setProperty("server.port", cliArgs.get("port"));
        }
        if (cliArgs.containsKey("debug")) {
            System.setProperty("logging.level.com.sessionflow", "DEBUG");
            System.setProperty("spring.jpa.show-sql", "true");
        } else if (cliArgs.containsKey("verbose")) {
            System.setProperty("logging.level.root", "INFO");
            System.setProperty("spring.jpa.show-sql", "true");
        } else {
            System.setProperty("logging.level.root", "WARN");
        }
    }
} 