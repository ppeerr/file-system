package per.demo.exception;

public class CreateFileException extends RuntimeException {
    public CreateFileException(String message) {
        super(message);
    }

    public CreateFileException(Throwable cause) {
        super(cause);
    }
}
