package dev.kkm.utils;


import dev.kkm.exception.FileStorageException;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * Utility class for path manipulation and validation.
 */
public class PathUtils {

    private PathUtils() {
        // Prevent instantiation
    }

    /**
     * Resolves a potentially user-provided relative path against the base directory.
     * Ensures the resolved path is still within the base directory.
     *
     * @param baseDir The application's base file storage directory.
     * @param userPath The user-provided relative path.
     * @return The resolved and validated absolute path.
     * @throws FileStorageException if the path is invalid, malicious (directory traversal),
     * or points outside the base directory.
     */
    public static Path resolveAndValidatePath(Path baseDir, String userPath) {
        if (baseDir == null) {
            throw new FileStorageException("Base directory is not configured.");
        }
        try {
            // Normalize the user path (e.g., remove redundant separators)
            Path normalizedUserPath = Paths.get(userPath).normalize();

            // Check for directory traversal attempts (e.g., starting with ../ or containing ..)
            if (normalizedUserPath.startsWith("..") || normalizedUserPath.toString().contains("..")) {
                throw new FileStorageException("Invalid path: Directory traversal attempt detected.");
            }
            // Also check if the path is absolute which is not allowed for user input
            if (normalizedUserPath.isAbsolute()) {
                throw new FileStorageException("Invalid path: Absolute paths are not allowed.");
            }


            // Resolve the user path against the base directory
            Path resolvedPath = baseDir.resolve(normalizedUserPath).normalize();

            // Verify that the resolved path is still within the base directory
            // This is the most robust check against directory traversal
            if (!resolvedPath.startsWith(baseDir)) {
                throw new FileStorageException("Invalid path: Resolved path is outside the base directory.");
            }

            return resolvedPath;

        } catch (InvalidPathException e) {
            throw new FileStorageException("Invalid path format: " + userPath);
        }
    }

    /**
     * Resolves the base directory path, ensuring it exists.
     *
     * @param baseDirPath The configured base directory path string.
     * @return The resolved absolute base directory Path.
     * @throws FileStorageException if the base directory path is invalid.
     */
    public static Path resolveBaseDir(String baseDirPath) {
        if (Objects.toString(baseDirPath,"").isBlank()) {
            throw new FileStorageException("Base directory path is not configured.");
        }
        try {
            // Resolve the base directory, make it absolute and normalize
            return Paths.get(baseDirPath).toAbsolutePath().normalize();
            // Note: We don't create the directory here; the service will do it later.
        } catch (InvalidPathException e) {
            throw new FileStorageException("Invalid base directory path format: " + baseDirPath);
        }
    }
}


