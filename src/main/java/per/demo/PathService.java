package per.demo;

import java.nio.file.FileSystem;
import java.util.Comparator;

public class PathService implements Comparator<InFilePath> {
    private volatile FileSystem fileSystem;
    private volatile InFilePath emptyPath;

    public void setFileSystem(FileSystem fileSystem) {
        this.fileSystem = fileSystem;
    }

    @Override
    public int compare(InFilePath paths, InFilePath t1) {
        return 0;
    }
}
