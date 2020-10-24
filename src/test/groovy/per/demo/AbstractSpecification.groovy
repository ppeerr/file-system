package per.demo

import spock.lang.Specification

abstract class AbstractSpecification extends Specification {
    protected static final String EXTENSION = ".iffs"
    protected static final String FILE_SYSTEM_CONTENT = "START\n" +
            "{\"kek\",2025,11,A};{\"kek1\",2037,12,A}                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            \n" +
            "----META ENDS----\n" +
            "Hello_world\n" +
            "Hello_world3"

    protected static destroySystemIfNotNull(InFileFileSystem system) {
        if (system != null)
            FileSystemFactory.destroy(system.getName())
    }

    protected static destroyStoreIfNotNull(InFileFileStore store) {
        if (store != null)
            store.destroy()
    }
}
