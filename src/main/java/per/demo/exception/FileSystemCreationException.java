package per.demo.exception;

import per.demo.model.Configuration;

public class FileSystemCreationException extends RuntimeException {
    public FileSystemCreationException(String name, Configuration configuration, Throwable cause) {
        super(
                String.format(
                        "Can't create FileSystem. Name=%s. Config=%s", name, configuration.toString()
                ),
                cause);
    }
}
