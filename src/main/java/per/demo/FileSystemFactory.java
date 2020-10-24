package per.demo;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class FileSystemFactory {

    private static final String EXTENSION = ".iffs";
    private static final ConcurrentMap<String, InFileFileSystem> INSTANCES = new ConcurrentHashMap<>();
    private static final Object updateInstancesLock = new Object();

    private FileSystemFactory() {
    }

    public static InFileFileSystem newFileSystem() {
        return newFileSystem(newRandomFileSystemName());
    }

    public static InFileFileSystem newFileSystem(String name) { //TODO synchronized?
        name += EXTENSION;

        InFileFileSystem system = INSTANCES.get(name);
        if (system != null) {
            return INSTANCES.get(name);
        }

        synchronized (updateInstancesLock) {
            system = INSTANCES.get(name);

            if (system == null) {
                system = new InFileFileSystem(name, createFileStore(name));
                INSTANCES.put(name, system);
            }

            return system;
        }
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
