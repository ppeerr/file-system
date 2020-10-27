package per.demo

import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Paths

abstract class AbstractSpecification extends Specification {
    protected static final String EXTENSION = ".iffs"
    protected static final String FILE_SYSTEM_CONTENT = "START\n" +
            "{\"kek\",2025,11,A}{\"kek1\",2037,12,A}                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             \n" +
            "----META ENDS----\n" +
            "Hello_world\n" +
            "Hello_world3"

    protected static final String CONTENT1 = "111111111111111111111111111111111111111111111111111111111111"
    protected static final String CONTENT2 = "222222222222222222222222222222222222222222222222222222222222"

    protected static destroySystemIfNotNull(InFileFileSystem system) {
        if (system != null && system.isOpen()) {
            FileSystemFactory.close(system.getName())
            Files.delete(Paths.get(system.getName()))
        }
    }

    protected static destroyStoreIfNotNull(InFileFileStore store) {
        if (store != null && store.isOpen()) {
            store.close()
            Files.delete(Paths.get(store.getFilePath()))
        }

    }
}
