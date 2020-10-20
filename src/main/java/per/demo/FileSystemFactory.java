package per.demo;

import java.util.UUID;

public final class FileSystemFactory {

    private FileSystemFactory() {
    }

    public static InFileFileSystem newFileSystem() {
        return newFileSystem(newRandomFileSystemName());
    }

    public static InFileFileSystem newFileSystem(String name) {
        InFileFileStore fileStore = createFileStore(name);

        return new InFileFileSystem(fileStore);
    }

    private static String newRandomFileSystemName() {
        return UUID.randomUUID().toString();
    }

    private static InFileFileStore createFileStore(String name) {
        return new InFileFileStore(name);
    }
}
