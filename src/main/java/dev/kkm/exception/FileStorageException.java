package dev.kkm.exception;


public class FileStorageException extends RuntimeException {

    private FileStorageException() {}

    public FileStorageException(String message) {
        super(message);
    }
}
