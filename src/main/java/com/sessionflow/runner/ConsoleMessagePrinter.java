package com.sessionflow.runner;

import java.io.PrintStream;

/**
 * Handles all console message formatting and printing for the SessionFlow application.
 */
public class ConsoleMessagePrinter {

    private static final String BORDER = "========================================================================";
    
    // ANSI Color codes
    private static final String RED = "\033[0;31m";
    private static final String GREEN = "\033[0;32m";
    private static final String YELLOW = "\033[1;33m";
    private static final String CYAN = "\033[0;36m";
    private static final String NC = "\033[0m"; // No Color

    /**
     * Prints the welcome message when the application starts successfully.
     */
    public static void printWelcomeMessage(int port, boolean webAppAssetsExist) {
        System.out.println("\n" + GREEN + BORDER + NC);
        System.out.println(GREEN + "  ✅ SessionFlow is running successfully!" + NC);
        System.out.println(GREEN + BORDER + NC);
        printAccessUrls(port, System.out, webAppAssetsExist);
        System.out.println(GREEN + BORDER + NC + "\n");
    }

    /**
     * Prints the message when another instance is already running.
     */
    public static void printAlreadyRunningMessage(String pid, int port) {
        System.err.println("\n" + YELLOW + BORDER + NC);
        System.err.println(YELLOW + "  ⚠️  SessionFlow is already running." + NC);
        if (pid != null && port != -1) {
            System.err.println(YELLOW + "     PID: " + RED + pid + NC + YELLOW + ", Port: " + RED + port + NC);
            System.err.println(YELLOW + BORDER + NC);
            System.err.println("  You can access the running instance at these URLs:\n");
            // Assume the running instance has web assets.
            printAccessUrls(port, System.err, true);
        } else {
            System.err.println(YELLOW + "     Could not determine the port of the running instance." + NC);
            System.err.println(YELLOW + BORDER + NC);
        }
        System.err.println();
    }

    /**
     * Prints the message when Web App assets are missing.
     */
    public static void printWebAppAssetsMissingMessage() {
        System.out.println("\n" + YELLOW + BORDER + NC);
        System.out.println(YELLOW + "  ℹ️  Web application assets seem to be missing." + NC);
        System.out.println(YELLOW + "      If you have just cloned the repository, you can download them" + NC);
        System.out.println(YELLOW + "      by running the following command from the project root:" + NC);
        System.out.println("        " + CYAN + "./scripts/update-webapp.sh" + NC);
        System.out.println(YELLOW + BORDER + NC + "\n");
    }

    /**
     * Prints the access URLs for the running application.
     */
    private static void printAccessUrls(int port, PrintStream stream, boolean webAppAssetsExist) {
        if (webAppAssetsExist) {
            stream.println("  " + CYAN + "Web App:       " + NC + "http://localhost:" + port + "/sessionflowapp" + "\n");
        }
        stream.println("  " + YELLOW + "API Docs:      " + NC + "http://localhost:" + port + "/swagger-ui.html");
        stream.println("  " + YELLOW + "H2 Console:    " + NC + "http://localhost:" + port + "/h2-console");
        stream.println("  " + YELLOW + "WebSocket URL: " + NC + "ws://localhost:" + port + "/ws");
    }
} 