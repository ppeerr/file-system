package per.demo.exception;

public class PutMetaForPresentFileException extends RuntimeException {
    public PutMetaForPresentFileException(String fileName) {
        super("Can't put meta ");
    }
}
