package per.demo;

import per.demo.exception.FileSystemCreationException;
import per.demo.model.Configuration;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Static factory methods for creating new InFile FileSystem instances. File systems may either be created
 * with a default configuration or by providinga specific {@link Configuration}.
 *
 * <p>Examples:
 *
 * <pre>
 *   // A file system with the default configuration
 *   FileSystem fileSystem = FileSystemFactory.newFileSystem(); </pre>
 *
 * <p>Additionally, the file system can be customized by creating a custom
 * {@link Configuration}. A new configuration can be created by lombok {@link Configuration#builder()}.
 * See {@link Configuration.ConfigurationBuilder} for what can be configured.
 *
 * <p>Examples:
 *
 * <pre>
 *   // Create with a custom configuration
 *   FileSystem fileSystem = FileSystemFactory.newFileSystem("specificName", Configuration.builder()
 *                 .metaHeader("START")
 *                 .metaDelimiter("--END--")
 *                 .metaBytesCount(5)
 *                 .build());  </pre>
 *
 * @author Anton Kupreychik
 */
public final class FileSystemFactory {

    private static final String EXTENSION = ".iffs";
    private static final ConcurrentMap<String, InFileFileSystem> INSTANCES = new ConcurrentHashMap<>();
    private static final Object UPDATE_INSTANCES_LOCK = new Object();

    private FileSystemFactory() {
    }

    /**
     * Creates a new InFile file system with a {@linkplain Configuration#defaultConfiguration()
     * default configuration} and random generated name {@linkplain FileSystemFactory#newRandomFileSystemName()}.
     */
    public static InFileFileSystem newFileSystem() {
        return newFileSystem(newRandomFileSystemName());
    }

    /**
     * Creates a new InFile file system with a {@linkplain Configuration#defaultConfiguration()
     * default configuration} but specific name.
     */
    public static InFileFileSystem newFileSystem(String name) {
        return newFileSystem(name, Configuration.defaultConfiguration());
    }

    /**
     * Creates a new inFile file system with the given configuration and name.
     *
     * <p>The factory return the same instance of InFileFileSystem for same file in OS if this instance has not been
     * closed by {@linkplain FileSystemFactory#close(String name)} or {@linkplain InFileFileSystem#close()}.
     *
     * If there is already an InFileFileSystem instance created by the factory, but it was closed previously by
     * instance method {@linkplain InFileFileSystem#close()}, then factory create new instance for the old file name
     * and create the InFileFileStore from existing file {@linkplain InFileFileStore#initializeFromFile()} ()}
     */
    public static InFileFileSystem newFileSystem(String name, Configuration configuration) {
        try {
            name += EXTENSION;

            InFileFileSystem system = INSTANCES.get(name);
            if (isSystemExistsAndOpen(system)) {
                return INSTANCES.get(name);
            }

            synchronized (UPDATE_INSTANCES_LOCK) {
                system = INSTANCES.get(name);

                if (isSystemDoesNotExistOrClosed(system)) {
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

    private static boolean isSystemExistsAndOpen(InFileFileSystem system) {
        return system != null && system.isOpen();
    }

    private static boolean isSystemDoesNotExistOrClosed(InFileFileSystem system) {
        return system == null || !system.isOpen();
    }
}
