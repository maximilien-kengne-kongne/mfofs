package dev.kkm.service;

import dev.kkm.exception.FileStorageException;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;


import java.nio.file.Path;
import java.util.List;
import java.util.Map;


/**
 * Service interface for managing files on the file system.
 * All paths are considered relative to the configured base directory,
 * unless specified otherwise (e.g., input streams).
 *
 * @version 1.0
 * @since 24/03/2025
 * @author <a href="mailto:maximiliendenver@gmail.com">maximilien kengne kongne</a>
 */
public interface FileStorageService {


    /**
     * Adds/uploads a file to a specific directory.
     * If the target directory does not exist, it will be created.
     *
     * @param file            The file to upload.
     * @param targetDirectory The path to the directory where the file should be saved, relative to the base directory.
     *                        Use "" or "/" for the root of the base directory.
     * @param filename name of file
     * @return The path where the file was saved, relative to the base directory.
     * @throws FileStorageException if an error occurs during saving or path validation.
     */
    String addFile(MultipartFile file, String targetDirectory, String filename) ;

    /**
     * Retrieves a file as a Spring Resource for serving/downloading.
     *
     * @param filePath The path to the file, relative to the base directory.
     * @return The file as a Resource.
     * @throws FileStorageException if the file does not exist or another error occurs during retrieval or path validation.
     */
    Resource getFileAsResource(String filePath) ;

    /**
     * Gets the Path object for a file or directory relative to the base directory.
     * Note: This method is primarily for internal use or specific advanced scenarios.
     * Use `getFileAsResource` for serving files.
     *
     * @param path The path to the file or directory, relative to the base directory.
     * @return The absolute Path object.
     * @throws FileStorageException if the path is invalid or points outside the base directory.
     */
    Path getPath(String path) ;


    /**
     * Lists files in a given directory.
     *
     * @param directoryPath The path to the directory, relative to the base directory. Use "" or "/" for the root.
     * @return A list of file names (not including directories).
     * @throws FileStorageException if the directory does not exist or an error occurs during listing or path validation.
     */
    List<String> listFiles(String directoryPath) ;

    /**
     * Lists subdirectories in a given directory.
     *
     * @param directoryPath The path to the directory, relative to the base directory. Use "" or "/" for the root.
     * @return A list of directory names (not including files).
     * @throws FileStorageException if the directory does not exist or an error occurs during listing or path validation.
     */
    List<String> listDirectories(String directoryPath);



/**
     * Lists all items (files and directories) in a given directory.
     *
     * @param directoryPath The path to the directory, relative to the base directory. Use "" or "/" for the root.
     * @return A list of item names (files and directories).
     * @throws FileStorageException if the directory does not exist or an error occurs during listing or path validation.
     */
    List<String> listItems(String directoryPath);

    /**
     * Creates a new directory, including any necessary but nonexistent parent directories.
     *
     * @param directoryPath The path to the directory to create, relative to the base directory.
     * @return The path of the created directory, relative to the base directory.
     * @throws FileStorageException if an error occurs during creation or path validation.
     */
    String createDirectory(String directoryPath) ;

    /**
     * Copies a file from a source path to a target path.
     * The target directory must exist.
     *
     * @param sourcePath The path to the source file, relative to the base directory.
     * @param targetPath The path where the file should be copied, relative to the base directory. This should be the destination file path, not a directory path.
     * @return The path where the file was copied, relative to the base directory.
     * @throws FileStorageException if the source file or target directory does not exist or an error occurs during copying or path validation.
     */
    String copyFile(String sourcePath, String targetPath) ;

    /**
     * Moves a file from a source path to a target path.
     * The target directory must exist.
     *
     * @param sourcePath The path to the source file, relative to the base directory. Ex: documents/file.txt
     * @param targetPath The path where the file should be moved, relative to the base directory.
     *                   This should be the destination file path, not a directory path. Ex: backups/file.txt
     * @return The path where the file was moved, relative to the base directory.
     * @throws FileStorageException if the source file or target directory does not exist or an error occurs during moving or path validation.
     */
    String moveFile(String sourcePath, String targetPath) ;

    /**
     * Renames an existing directory by moving it to a new path with a different name.
     * <p>
     * This operation is equivalent to moving the directory to a new location. It fails if the source
     * directory does not exist or if the target directory already exists.
     *
     * @param oldDirectory the relative path (from the storage root) of the directory to rename
     * @param newDirectory the relative path (from the storage root) of the renamed directory
     * @throws FileStorageException if the source directory does not exist or is not a directory,
     * or the target directory already exists or an error occurs during the rename operation.
     */
    void renameDirectory(String oldDirectory, String newDirectory);

    /**
     * Deletes a file or an empty directory.
     *
     * @param path The path to the file or directory to delete, relative to the base directory.
     * @throws FileStorageException if the file or directory does not exist or the directory is not empty or another error occurs during deletion or path validation.
     */
    void delete(String path) ;

    /**
     * Deletes a file or a directory, including its contents (recursively).
     * Use with caution!
     *
     * @param path The path to the item to delete, relative to the base directory.
     * @throws FileStorageException if the item does not exist or an error occurs during deletion or path validation.
     */
    void deleteRecursive(String path);

    /**
     * Checks if an item (file or directory) exists at the given path.
     *
     * @param path The path to the item, relative to the base directory.
     * @return true if the item exists, false otherwise.
     * @throws FileStorageException if an error occurs during path validation.
     */
    boolean exists(String path);

    /**
     * Checks if the item at the given path is a directory.
     *
     * @param path The path to the item, relative to the base directory.
     * @return true if the item exists and is a directory, false otherwise.
     * @throws FileStorageException if an error occurs during path validation.
     */
    boolean isDirectory(String path);

    /**
     * Checks if the item at the given path is a regular file.
     *
     * @param path The path to the item, relative to the base directory.
     * @return true if the item exists and is a regular file, false otherwise.
     * @throws FileStorageException if an error occurs during path validation.
     */
    boolean isFile(String path);

    /**
     * Gets the size of a file in bytes.
     *
     * @param filePath The path to the file, relative to the base directory.
     * @return The size of the file in bytes.
     * @throws FileStorageException if the file does not exist or is not a file or an error occurs during size retrieval or path validation.
     */
    long getFileSize(String filePath);

    Map<String, List<String>> currentTree();
}