package per.demo

import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

class InFileFileStoreTest extends AbstractSpecification {

    private InFileFileStore fileStore

    def "should create FileSystem with valid from existent file"() {
        given:
        def name = "FROM_FILE"
        def file = Files.createFile(Paths.get(name + EXTENSION))
        Files.writeString(file, FILE_SYSTEM_CONTENT, StandardOpenOption.WRITE)

        when:
        fileStore = new InFileFileStore(name + EXTENSION, Configuration.defaultConfiguration())

        then:
        fileStore
        fileStore.getMetaContent().trim() == "{\"kek\",2025,11,A}{\"kek1\",2037,12,A}"
    }

    void cleanup() {
        destroyStoreIfNotNull(fileStore)
    }
}
