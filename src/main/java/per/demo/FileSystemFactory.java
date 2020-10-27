package per.demo;

import per.demo.exception.FileSystemCreationException;
import per.demo.model.Configuration;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class FileSystemFactory {

    private static final String EXTENSION = ".iffs";
    private static final ConcurrentMap<String, InFileFileSystem> INSTANCES = new ConcurrentHashMap<>();
    private static final Object UPDATE_INSTANCES_LOCK = new Object();

    private FileSystemFactory() {
    }

    public static InFileFileSystem newFileSystem() {
        return newFileSystem(newRandomFileSystemName());
    }

    public static InFileFileSystem newFileSystem(String name) {
        return newFileSystem(name, Configuration.defaultConfiguration());
    }

    public static InFileFileSystem newFileSystem(String name, Configuration configuration) {
        try {
            name += EXTENSION;

            InFileFileSystem system = INSTANCES.get(name);
            if (system != null && system.isOpen()) {
                return INSTANCES.get(name);
            }

            synchronized (UPDATE_INSTANCES_LOCK) {
                system = INSTANCES.get(name);

                if (system == null || !system.isOpen()) {
                    system = new InFileFileSystem(
                            name,
                            new InFileFileStore(name, configuration),
                            new InFileFileStoreView()
                    );
                    INSTANCES.put(name, system);
                }

                return system;
            }
        } catch (Exception e) {
            throw new FileSystemCreationException(name, configuration, e);
        }
    }

    public static void close(String name) {
        InFileFileSystem system = INSTANCES.get(name);
        if (system == null) {
            return;
        }

        system.close();
        INSTANCES.remove(name);
    }

    private static String newRandomFileSystemName() {
        return UUID.randomUUID().toString();
    }
}
