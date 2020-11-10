package per.demo.exception;

import per.demo.model.Configuration;

import java.nio.file.Path;

public class FileSystemCreationException extends RuntimeException {

    public FileSystemCreationException(String name, Throwable cause) {
        super(
                String.format("Can't create FileSystem. Name=%s", name),
                cause);
    }

    public FileSystemCreationException(Path file, Configuration configuration, Throwable cause) {
        super(
                String.format(
                        "Can't create FileSystem. Name=%s. Config=%s", file, configuration.toString()
                ),
                cause);
    }
}
