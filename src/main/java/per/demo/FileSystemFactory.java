package per.demo;

import per.demo.exception.FileSystemCreationException;
import per.demo.extendable.ExtendableInFileFileStore;
import per.demo.extendable.InFileFileStoreViewImpl;
import per.demo.extendable.InFileFileSystemImpl;
import per.demo.model.Configuration;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Factory methods for creating new InFile FileSystem instances. File systems may either be created
 * with a default configuration or by providing specific {@link Configuration}.
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

    private final ConcurrentMap<String, InFileFileSystemImpl> INSTANCES = new ConcurrentHashMap<>();
    private final Object UPDATE_INSTANCES_LOCK = new Object();

    /**
     * Creates a new InFile file system with default config {@linkplain Configuration#defaultConfiguration()}
     * and random generated name {@linkplain FileSystemFactory#newRandomFileSystemName()}.
     *
     * @return new InFileFileSystem object
     */
    public InFileFileSystem newFileSystem() {
        return newFileSystem(newRandomFileSystemName());
    }

    /**
     * Creates a new InFile file system with default config {@linkplain Configuration#defaultConfiguration()}
     * and given name.
     *
     * @param name name for new InFileFileSystem
     * @return new InFileFileSystem object
     */
    public InFileFileSystem newFileSystem(String name) {
        return newFileSystem(name, Configuration.defaultConfiguration());
    }

    /**
     * Creates a new InFile file system with given config and name.
     *
     * @param name name for new InFileFileSystem
     * @param config configuration for new InFileFileSystem (see {@linkplain Configuration})
     * @return new InFileFileSystem object
     */
    public InFileFileSystem newFileSystem(String name, Configuration config) {
        try {
            Path file = Paths.get(name + EXTENSION);
            return newFileSystem(file, config);
        } catch (Exception e) {
            throw new FileSystemCreationException(name, e);
        }
    }

    /**
     * Creates a new inFile file system with the given configuration and file path.
     *
     * <p>The factory return the same instance of InFileFileSystem for the same file in OS created by this factory
     * if this instance has not been closed by {@linkplain InFileFileSystem#close()}.
     * <p>
     * If there is already an InFileFileSystem instance created by the factory, but it was closed previously by
     * instance method {@linkplain InFileFileSystem#close()}, then factory create new instance for the old file
     * and create the InFileFileStore from existing file
     *
     * @param file {@linkplain Path} file for new InFileFileSystem
     * @param config configuration for new InFileFileSystem (see {@linkplain Configuration})
     * @return new InFileFileSystem object
     */
    public InFileFileSystem newFileSystem(Path file, Configuration config) {
        try {
            String name = file.toString();

            InFileFileSystemImpl system = INSTANCES.get(name);
            if (isSystemExistsAndOpen(system)) {
                return INSTANCES.get(name);
            }

            synchronized (UPDATE_INSTANCES_LOCK) {
                system = INSTANCES.get(name);

                if (isSystemDoesNotExistOrClosed(system)) {
                    system = new InFileFileSystemImpl(
                            name,
                            new ExtendableInFileFileStore(file, config),
                            new InFileFileStoreViewImpl()
                    );
                    INSTANCES.put(name, system);
                }

                return system;
            }
        } catch (Exception e) {
            throw new FileSystemCreationException(file, config, e);
        }
    }

    private static String newRandomFileSystemName() {
        return UUID.randomUUID().toString();
    }

    private static boolean isSystemExistsAndOpen(InFileFileSystemImpl system) {
        return system != null && system.isOpen();
    }

    private static boolean isSystemDoesNotExistOrClosed(InFileFileSystemImpl system) {
        return system == null || !system.isOpen();
    }
}
