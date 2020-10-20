/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package per.demo;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.UUID;

public final class FileSystemFactory {

    public static final String URI_SCHEME = "kekfs";

    private FileSystemFactory() {
    }

    public static InFileFileSystem newFileSystem() throws URISyntaxException, IOException {
        return newFileSystem(newRandomFileSystemName());
    }

    public static InFileFileSystem newFileSystem(String name) throws IOException {
//        PathService pathService = new PathService();
//        FileSystemState state = new FileSystemState();

        InFileFileStore fileStore = createFileStore(name);
//        FileSystemView defaultView = createDefaultView(config, fileStore, pathService);

        InFileFileSystem fileSystem = new InFileFileSystem(fileStore);

//        pathService.setFileSystem(fileSystem);
        return fileSystem;
    }

    private static String newRandomFileSystemName() {
        return UUID.randomUUID().toString();
    }

    private static InFileFileStore createFileStore(String name) {
        return new InFileFileStore(name);
    }

    /**
     * Creates the default view of the file system using the given working directory.
     */
    private static FileSystemView createDefaultView(
            Configuration config,
            InFileFileStore fileStore,
            PathService pathService
    ) throws IOException {
        return new FileSystemView(fileStore, new Directory(), new InFilePath());
    }
}