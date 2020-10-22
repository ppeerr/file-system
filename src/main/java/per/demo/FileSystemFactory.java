package per.demo;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class FileSystemFactory {

    private static final String EXTENSION = ".iffs";
    private static Map<String, InFileFileSystem> INSTANCES = new ConcurrentHashMap<>();

    private FileSystemFactory() {
    }

    public static InFileFileSystem newFileSystem() {
        return newFileSystem(newRandomFileSystemName());
    }

    public static InFileFileSystem newFileSystem(String name) { //TODO synchronized?
        name += EXTENSION;

        if (INSTANCES.containsKey(name)) {
            return INSTANCES.get(name);
        }

        InFileFileStore fileStore = createFileStore(name);
        InFileFileSystem system = new InFileFileSystem(name, fileStore);

        INSTANCES.put(name, system);
        return system;
    }

    public static void destroy(String name) {
        InFileFileSystem system = INSTANCES.get(name);
        if (system == null) {
            return;
        }

        system.destroy();
        INSTANCES.remove(name);
    }

    private static String newRandomFileSystemName() {
        return UUID.randomUUID().toString();
    }

    private static InFileFileStore createFileStore(String name) {
        return new InFileFileStore(name);
    }
}
