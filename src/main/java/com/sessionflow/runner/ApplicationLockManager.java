package com.sessionflow.runner;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.StandardCharsets;

/**
 * Manages application instance locking to ensure only one instance runs at a time.
 */
public class ApplicationLockManager {

    private static final String LOCK_FILE_NAME = ".sessionflow.lock";
    private static final String INFO_FILE_NAME = ".sessionflow.info";

    private final File lockFile;
    private final File infoFile;
    private RandomAccessFile lockRaf;
    private FileChannel lockChannel;
    private FileLock fileLock;

    public ApplicationLockManager() {
        this.lockFile = new File(LOCK_FILE_NAME);
        this.infoFile = new File(INFO_FILE_NAME);
    }

    /**
     * Attempts to acquire the application lock.
     * 
     * @return true if lock was acquired successfully, false if another instance is running
     * @throws IOException if there's an error with file operations
     */
    public boolean acquireLock() throws IOException {
        lockRaf = new RandomAccessFile(lockFile, "rw");
        lockChannel = lockRaf.getChannel();
        fileLock = lockChannel.tryLock();

        if (fileLock == null) {
            // Another instance is running
            lockChannel.close();
            lockRaf.close();
            handleAlreadyRunning();
            return false;
        }

        // Lock acquired, write instance info
        writeInstanceInfo();
        setupShutdownHook();
        return true;
    }

    /**
     * Writes the current instance information (PID and port) to the info file.
     */
    private void writeInstanceInfo() throws IOException {
        String port = System.getProperty("server.port", "53551");
        long pid = ProcessHandle.current().pid();
        String lockContent = pid + ":" + port;

        try (RandomAccessFile infoRaf = new RandomAccessFile(infoFile, "rw")) {
            infoRaf.setLength(0);
            infoRaf.write(lockContent.getBytes(StandardCharsets.UTF_8));
        }
    }

    /**
     * Handles the case when another instance is already running.
     */
    private void handleAlreadyRunning() throws IOException {
        InstanceInfo info = readInstanceInfo();
        ConsoleMessagePrinter.printAlreadyRunningMessage(info.pid, info.port);
    }

    /**
     * Reads instance information from the info file.
     */
    private InstanceInfo readInstanceInfo() throws IOException {
        String pid = null;
        int port = -1;

        if (infoFile.exists()) {
            try (RandomAccessFile infoRaf = new RandomAccessFile(infoFile, "r")) {
                String lockContent = infoRaf.readLine();
                if (lockContent != null) {
                    String[] parts = lockContent.split(":");
                    if (parts.length == 2) {
                        pid = parts[0];
                        try {
                            port = Integer.parseInt(parts[1]);
                        } catch (NumberFormatException e) {
                            port = -1;
                        }
                    }
                }
            }
        }

        return new InstanceInfo(pid, port);
    }

    /**
     * Sets up the shutdown hook to clean up resources when the application exits.
     */
    private void setupShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::releaseLock));
    }

    /**
     * Releases the lock and cleans up resources.
     */
    private void releaseLock() {
        try {
            if (fileLock != null && fileLock.isValid()) {
                fileLock.release();
            }
            if (lockChannel != null && lockChannel.isOpen()) {
                lockChannel.close();
            }
            if (lockRaf != null) {
                lockRaf.close();
            }
            if (lockFile.exists()) {
                lockFile.delete();
            }
            if (infoFile.exists()) {
                infoFile.delete();
            }
        } catch (IOException e) {
            System.err.println("Error during shutdown hook: " + e.getMessage());
        }
    }

    /**
     * Cleans up resources in case of initialization failure.
     */
    public void cleanup() {
        try {
            if (fileLock != null && fileLock.isValid()) {
                fileLock.release();
            }
            if (lockChannel != null && lockChannel.isOpen()) {
                lockChannel.close();
            }
            if (lockRaf != null) {
                lockRaf.close();
            }
        } catch (IOException e) {
            System.err.println("Error during cleanup: " + e.getMessage());
        }
    }

    /**
     * Simple data class to hold instance information.
     */
    private static class InstanceInfo {
        final String pid;
        final int port;

        InstanceInfo(String pid, int port) {
            this.pid = pid;
            this.port = port;
        }
    }
} 