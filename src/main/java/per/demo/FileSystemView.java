package per.demo;

public class FileSystemView {
    private final InFileFileStore store;

    private final Directory workingDirectory;
    private final InFilePath workingDirectoryPath;

    public FileSystemView(InFileFileStore store, Directory workingDirectory, InFilePath workingDirectoryPath) {
        this.store = store;
        this.workingDirectory = workingDirectory;
        this.workingDirectoryPath = workingDirectoryPath;
    }
}
