package com.clientjourney.storage.file;

public class CorruptedStorageException extends RuntimeException {
    public CorruptedStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
