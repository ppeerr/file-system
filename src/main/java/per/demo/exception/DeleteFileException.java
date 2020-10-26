package per.demo.exception;

public class DeleteFileException extends RuntimeException {
    public DeleteFileException(String message) {
        super(message);
    }

    public DeleteFileException(Throwable cause) {
        super(cause);
    }
}
