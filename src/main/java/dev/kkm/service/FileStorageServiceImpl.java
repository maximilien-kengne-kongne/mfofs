package dev.kkm.service;

import dev.kkm.exception.FileStorageException;
import dev.kkm.utils.PathUtils;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Implementation of FileSystemService using java.nio.file.
 * Manages files within the configured base directory.
 */
public class FileStorageServiceImpl implements FileStorageService {

    @Value("${file.storage.base-dir:./uploads}")
    private String baseDir;
    private Path baseDirPath;
    private static final Logger log = LoggerFactory.getLogger(FileStorageServiceImpl.class);

    /**
     * Initializes the base directory path and creates it if it doesn't exist.
     */
    @PostConstruct
    public void init() {
        try {
            baseDirPath = PathUtils.resolveBaseDir(baseDir);
            // Create the base directory if it doesn't exist
            if (!Files.exists(baseDirPath)) {
                Files.createDirectories(baseDirPath);
                log.info("Created base directory: {}", baseDirPath);
            } else if (!Files.isDirectory(baseDirPath)) {
                throw new FileStorageException("Configured base directory is not a directory: " + baseDirPath);
            }
        } catch (IOException e) {
            throw new FileStorageException("Could not initialize base directory: " + baseDir);
        }
        log.info("Using base directory: {}", baseDirPath);
    }


    @Override
    public String addFile(MultipartFile file, String targetDirectory, String filename) {
        log.info("Adding file: {} to targetDirectory {}", filename, targetDirectory);

        if (file.isEmpty()) {
            throw new FileStorageException("Cannot store empty file.");
        }

        if (Objects.toString(targetDirectory, "").isBlank()) {
            targetDirectory = baseDir; // Default to root
        }

        validateEntry(filename);

        try {
            // Resolve the target directory path and ensure it's within the base directory
            Path targetDirPath = resolveAndValidate(targetDirectory);

            // Ensure the target path is actually a directory
            if (Files.exists(targetDirPath) && !Files.isDirectory(targetDirPath)) {
                throw new FileStorageException("Target path '" + targetDirectory + "' is not a directory.");
            }

            // Create target directory if it doesn't exist
            Files.createDirectories(targetDirPath);

            // Resolve the full target file path
            // Sanitize the original filename to prevent issues (e.g., weird characters)
            String originalFilename = StringUtils.cleanPath(filename);
            if (originalFilename.contains("..")) {
                // This check is technically redundant if PathUtils is perfect, but good layered defense
                throw new FileStorageException("Cannot store file with relative path segments (..)");
            }

            Path targetFilePath = targetDirPath.resolve(originalFilename);

            // Copy the file's input stream to the target file path
            Files.copy(file.getInputStream(), targetFilePath, StandardCopyOption.REPLACE_EXISTING);

            // Return the relative path from the base directory
            return baseDirPath.relativize(targetFilePath).toString();

        } catch (Exception e) {
            throw new FileStorageException(e.getMessage());
        }
    }

    @Override
    public Resource getFileAsResource(String filePath) {
        try {
            Path file = resolveAndValidate(filePath);
            if (!Files.exists(file) || !Files.isRegularFile(file)) {
                throw new FileStorageException(filePath);
            }
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new FileStorageException(filePath); // Resource might exist but not readable
            }
        } catch (Exception e) {
            throw new FileStorageException(e.getMessage() + filePath);
        }
    }

    @Override
    public Path getPath(String path) {
        return resolveAndValidate(path);
    }


    @Override
    public List<String> listFiles(String directoryPath) {
        return listItemsInternal(directoryPath, Files::isRegularFile);
    }

    @Override
    public List<String> listDirectories(String directoryPath) {
        return listItemsInternal(directoryPath, Files::isDirectory);
    }

    @Override
    public List<String> listItems(String directoryPath) throws FileStorageException {
        return listItemsInternal(directoryPath,entry -> true); // List all entries
    }


    @Override
    public String createDirectory(String directoryPath) {
        log.info("Creating directory: {}", directoryPath);

        if (Objects.toString(directoryPath, "").isBlank() ) {
            throw new FileStorageException("Directory path cannot be empty.");
        }

        try {
            Path dirToCreate = resolveAndValidate(directoryPath);

            // Check if it already exists and is a directory
            if (Files.exists(dirToCreate)) {
                if (Files.isDirectory(dirToCreate)) {
                    // Directory already exists, which might be okay depending on requirements.
                    // Here we'll just return the path, assuming idempotency is desired.
                    return baseDirPath.relativize(dirToCreate).toString();
                } else {
                    // Exists but is a file
                    throw new FileStorageException("Cannot create directory '" + directoryPath + "'. A file already exists at this path.");
                }
            }

            Files.createDirectories(dirToCreate);
            return baseDirPath.relativize(dirToCreate).toString();
        } catch (Exception e) {
            throw new FileStorageException(e.getMessage() + directoryPath);
        }
    }

    @Override
    public String copyFile(String sourcePath, String targetPath) {
        log.info("Copying file {} to {}", sourcePath, targetPath);

        if (Objects.toString(sourcePath, "").isBlank() || Objects.toString(targetPath, "").isBlank()) {
            throw new FileStorageException("Source or target paths cannot be empty.");
        }
        try {
            Path source = resolveAndValidate(sourcePath);
            Path target = resolveAndValidate(targetPath);

            if (!Files.exists(source) || !Files.isRegularFile(source)) {
                throw new FileStorageException("Source file not found or is not a file: " + sourcePath);
            }

            // Ensure the target parent directory exists
            Path targetParent = target.getParent();
            if (targetParent == null || !Files.exists(targetParent) || !Files.isDirectory(targetParent)) {

                Path resolvedTargetParent = target.getParent(); // Parent of the target file path
                if (resolvedTargetParent == null || !Files.exists(resolvedTargetParent) || !Files.isDirectory(resolvedTargetParent)) {
                    throw new FileStorageException("Target directory for '" + targetPath + "' does not exist.");
                }
            }

            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING); // Option to overwrite if target exists
            return baseDirPath.relativize(target).toString();

        } catch (Exception e) {
            throw new FileStorageException(e.getMessage());
        }
    }

    @Override
    public String moveFile(String sourcePath, String targetPath) {
        log.info("Moving file {} to {}", sourcePath, targetPath);

        if (Objects.toString(sourcePath, "").isBlank() || Objects.toString(targetPath, "").isBlank()) {
            throw new FileStorageException("Source or target paths cannot be empty.");
        }

        try {
            Path source = resolveAndValidate(sourcePath);
            Path target = resolveAndValidate(targetPath);

            if (!Files.exists(source) || !Files.isRegularFile(source)) {
                throw new FileStorageException("Source file not found or is not a file: " + sourcePath);
            }

            // Ensure the target parent directory exists (same logic as copy)
            Path resolvedTargetParent = target.getParent();
            if (resolvedTargetParent == null || !Files.exists(resolvedTargetParent) || !Files.isDirectory(resolvedTargetParent)) {
                throw new FileStorageException("Target directory for '" + targetPath + "' does not exist.");
            }


            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING); // Option to overwrite if target exists
            return baseDirPath.relativize(target).toString();

        } catch (Exception e) {
            throw new FileStorageException(e.getMessage());
        }
    }

    @Override
    public void renameDirectory(String oldDirectory, String newDirectory)  {

        if (Objects.toString(oldDirectory, "").isBlank() || Objects.toString(newDirectory, "").isBlank()) {
            throw new FileStorageException("Old or new directories cannot be empty.");
        }

        Path sourcePath = resolveAndValidate(oldDirectory);
        Path targetPath = resolveAndValidate(newDirectory);

        if (!Files.exists(sourcePath) || !Files.isDirectory(sourcePath)) {
            throw new FileStorageException("The directory to rename doesn't exist : " + oldDirectory);
        }

        if (Files.exists(targetPath)) {
            throw new FileStorageException("The new directory already exist : " + newDirectory);
        }

        try {
            Files.move(sourcePath, targetPath);
        } catch (IOException e) {
            throw new FileStorageException(e.getMessage());
        }
    }


    @Override
    public void delete(String path) {
        log.info("Deleting file " + path);
        if (Objects.toString(path, "").isBlank()) {
            throw new FileStorageException("Path to delete cannot be empty.");
        }

        try {
            Path itemToDelete = resolveAndValidate(path);

            if (!Files.exists(itemToDelete)) {
                throw new FileStorageException(path);
            }

            // Prevent deleting the base directory itself via this method
            if (itemToDelete.equals(baseDirPath)) {
                throw new FileStorageException("Cannot delete the base directory itself using the delete method.");
            }

            Files.delete(itemToDelete); // This will fail for non-empty directories
        } catch (Exception e) {
            throw new FileStorageException(e.getMessage());
        }
    }

    @Override
    public void deleteRecursive(String path) throws FileStorageException {

        if (Objects.toString(path, "").isBlank()) {
            throw new FileStorageException("Path to delete cannot be empty.");
        }

        try {
            Path itemToDelete = resolveAndValidate(path);

            if (!Files.exists(itemToDelete)) {
                throw new FileStorageException(path);
            }

            // Prevent deleting the base directory itself via this method (recursive or not)
            if (itemToDelete.equals(baseDirPath)) {
                throw new FileStorageException("Cannot delete the base directory itself using the deleteRecursive method.");
            }


            // Recursively delete directory contents first, then the directory itself
            // This is safer than walking and deleting items one by one which can fail mid-way
            Files.walk(itemToDelete)
                    .sorted(Comparator.reverseOrder()) // Process descendants before ancestors
                    .forEach(p -> {
                        try {
                            Files.delete(p);
                        } catch (IOException e) {
                            // Wrap IOException in FileStorageException and re-throw
                            throw new FileStorageException("Failed to delete path during recursive deletion: " + p);
                        }
                    });

        } catch (Exception e) {
            throw new FileStorageException(e.getMessage());
        }
    }

    @Override
    public boolean exists(String path) {
        try {
            Path itemPath = resolveAndValidate(path);
            // existence check doesn't need the item to be in the base directory
            // after validation, we just check if the resolved path exists on disk.
            return Files.exists(itemPath);
        } catch (Exception e) {
            throw new FileStorageException("An unexpected error occurred while checking existence.");
        }
    }

    @Override
    public boolean isDirectory(String path) throws FileStorageException {
        try {
            Path itemPath = resolveAndValidate(path);
            return Files.isDirectory(itemPath);
        } catch (Exception e) {
            throw new FileStorageException(e.getMessage());
        }
    }

    @Override
    public boolean isFile(String path) throws FileStorageException {
        try {
            Path itemPath = resolveAndValidate(path);
            return Files.isRegularFile(itemPath);
        } catch (Exception e) {
            throw new FileStorageException(e.getMessage());
        }
    }

    @Override
    public long getFileSize(String filePath) throws FileStorageException {
        try {
            Path file = resolveAndValidate(filePath);

            if (!Files.exists(file) || !Files.isRegularFile(file)) {
                throw new FileStorageException(filePath);
            }

            return Files.size(file);

        } catch (Exception e) {
            throw new FileStorageException(e.getMessage() + filePath);
        }
    }

    @Override
    public Map<String, List<String>> currentTree() {
        log.info("Current tree:");
        Map<String, List<String>> tree = new HashMap<>();

        try {
            Files.walk(baseDirPath).filter(Files::isDirectory).forEach(dir -> {
                try(Stream<Path> children = Files.list(dir)) {
                    String relative = baseDirPath.relativize(dir).toString().replace("\\", "/");
                    List<String> items = children.map(path -> path.getFileName().toString()).toList();
                    tree.put(relative.isEmpty()?"":relative, items);

                }catch (IOException e) {
                    throw new FileStorageException(e.getMessage());
                }
            });
        } catch (Exception e) {
            throw new FileStorageException(e.getMessage());
        }
        return tree;
    }

    private List<String> listItemsInternal(String directoryPath, DirectoryStream.Filter<Path> filter) {
        Path dir;
        try {
            dir = resolveAndValidate(directoryPath);
        } catch (FileStorageException e) {
            // If the directory path validation fails, it's a file system issue or bad path.
            // But if the *resolved* path doesn't exist, that's a FileNotFound.
            // Check existence after validation but before trying to list.
            Path potentialDir = PathUtils.resolveAndValidatePath(baseDirPath, Objects.toString(directoryPath, "").isBlank() ? "/" : directoryPath);
            if (!Files.exists(potentialDir)) {
                throw new FileStorageException("File not found" + directoryPath);
            }
            // If it exists but isn't a directory, throw error
            if (!Files.isDirectory(potentialDir)) {
                throw new FileStorageException("Path '" + directoryPath + "' is not a directory.");
            }
            // If validation failed for other reasons (e.g., traversal), re-throw the original exception
            throw e;
        }


        if (!Files.exists(dir)) {
            throw new FileStorageException(directoryPath);
        }
        if (!Files.isDirectory(dir)) {
            throw new FileStorageException("Path '" + directoryPath + "' is not a directory.");
        }


        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, filter)) {
            return StreamSupport.stream(stream.spliterator(), false)
                    .map(Path::getFileName) // Get just the name part
                    .map(Path::toString)
                    .toList();
        } catch (IOException e) {
            throw new FileStorageException("Failed to list items in directory: " + directoryPath);
        } catch (Exception e) {
            throw new FileStorageException("An unexpected error occurred while listing items.");
        }
    }

    /**
     * Resolves a user-provided relative path against the base directory
     * and validates that it stays within the base directory bounds.
     *
     * @param userPath The path provided by the user (relative to base).
     * @return The validated absolute Path object.
     * @throws FileStorageException if the path is invalid or outside the base directory.
     */
    private Path resolveAndValidate(String userPath) {
        // Handle null or empty path for root operations
        String cleanedUserPath = Objects.toString(userPath, "").isBlank() ? "/" : userPath;
        return PathUtils.resolveAndValidatePath(baseDirPath, cleanedUserPath);
    }

    private void validateEntry(String entry) {
        if (Objects.toString(entry, "").isBlank()) {
            throw new FileStorageException(entry + " cannot be null or empty");
        }
    }
}
