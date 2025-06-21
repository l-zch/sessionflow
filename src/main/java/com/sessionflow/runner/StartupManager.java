package com.sessionflow.runner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.web.context.WebServerApplicationContext;
import org.springframework.context.ApplicationListener;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class StartupManager {

    private static final String LOCK_FILE_NAME = ".sessionflow.lock";

    public static void run(Class<?> primarySource, String[] args) {
        // 1. Parse command-line arguments
        Map<String, String> cliArgs = parseArguments(args);
        applyCliArguments(cliArgs);

        // 2. Attempt to acquire file lock
        try {
            File lockFile = new File(LOCK_FILE_NAME);
            RandomAccessFile randomAccessFile = new RandomAccessFile(lockFile, "rw");
            FileChannel fileChannel = randomAccessFile.getChannel();
            FileLock fileLock = fileChannel.tryLock();

            if (fileLock == null) {
                // Instance is already running
                printAlreadyRunningMessage(randomAccessFile);
                fileChannel.close();
                randomAccessFile.close();
                System.exit(1);
            }

            // 3. Lock acquired, write PID and Port to lock file
            String port = System.getProperty("server.port", "8080");
            long pid = ProcessHandle.current().pid();
            String lockContent = pid + ":" + port;

            randomAccessFile.setLength(0);
            randomAccessFile.write(lockContent.getBytes(StandardCharsets.UTF_8));

            // 4. Add shutdown hook to release lock and delete file
            addShutdownHook(lockFile, fileLock, fileChannel, randomAccessFile);

            // 5. Proceed with application startup
            SpringApplication app = new SpringApplication(primarySource);
            app.addListeners((ApplicationListener<ApplicationReadyEvent>) event -> {
                WebServerApplicationContext context = (WebServerApplicationContext) event.getApplicationContext();
                printWelcomeMessage(context.getWebServer().getPort());
            });

            app.run(args);

        } catch (IOException e) {
            System.err.println("❌ Could not create or lock the file '" + LOCK_FILE_NAME + "'.");
            e.printStackTrace();
            System.exit(1);
        }
    }

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

    private static void addShutdownHook(File lockFile, FileLock fileLock, FileChannel channel, RandomAccessFile raf) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                if (fileLock != null) {
                    fileLock.release();
                }
                channel.close();
                raf.close();
                lockFile.delete();
                System.out.println("\nℹ️  SessionFlow shutting down. Lock file released and deleted.");
            } catch (IOException e) {
                System.err.println("Error during shutdown hook: " + e.getMessage());
            }
        }));
    }

    private static void printAlreadyRunningMessage(RandomAccessFile randomAccessFile) throws IOException {
        // Read PID and Port from the existing lock file
        randomAccessFile.seek(0);
        String lockContent = randomAccessFile.readLine();
        String[] parts = lockContent != null ? lockContent.split(":") : new String[0];

        String red = "\033[0;31m";
        String yellow = "\033[1;33m";
        String nc = "\033[0m";

        System.err.println("\n" + yellow + "========================================================================" + nc);
        System.err.println(yellow + "  ⚠️  SessionFlow is already running." + nc);
        if (parts.length == 2) {
            String pid = parts[0];
            int port = Integer.parseInt(parts[1]);
            System.err.println(yellow + "     PID: " + red + pid + nc + yellow + ", Port: " + red + port + nc);
            System.err.println(yellow + "========================================================================" + nc);
            System.err.println("  You can access the running instance at these URLs:\n");
            printAccessUrls(port, System.err); // Print URLs to stderr
        } else {
            System.err.println(yellow + "     Could not determine the port of the running instance." + nc);
            System.err.println(yellow + "========================================================================" + nc);
        }
        System.err.println();
    }

    private static void printWelcomeMessage(int port) {
        String border = "========================================================================";
        String green = "\033[0;32m";
        String nc = "\033[0m";

        System.out.println("\n" + green + border + nc);
        System.out.println(green + "  ✅ SessionFlow is running successfully!" + nc);
        System.out.println(green + border + nc);
        printAccessUrls(port, System.out);
        System.out.println(green + border + nc + "\n");
    }

    private static void printAccessUrls(int port, PrintStream stream) {
        String yellow = "\033[1;33m";
        String cyan = "\033[0;36m";
        String nc = "\033[0m";
        stream.println("  " + cyan + "Web App:       " + nc + "http://localhost:" + port + "/sessionflowapp" +"\n");
        stream.println("  " + yellow + "API Docs:      " + nc + "http://localhost:" + port + "/swagger-ui.html");
        stream.println("  " + yellow + "H2 Console:    " + nc + "http://localhost:" + port + "/h2-console");
        stream.println("  " + yellow + "WebSocket URL: " + nc + "ws://localhost:" + port + "/ws");
    }
} 