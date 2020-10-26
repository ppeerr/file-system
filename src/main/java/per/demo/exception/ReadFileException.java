package per.demo.exception;

public class ReadFileException extends RuntimeException {
    public ReadFileException(String message) {
        super(message);
    }

    public ReadFileException(String fileName, Throwable cause) {
        super("Read file '" + fileName +"' failed", cause);
    }
}
