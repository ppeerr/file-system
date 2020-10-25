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
        def map = fileStore.getPositionsAndSizesByNames()

        then:
        map.size() == 2
        map["kek"] == new MetaInfo(2025, 11, 21).setPresent(true)
        map["kek1"] == new MetaInfo(2037, 12, 40).setPresent(true)
    }

    void cleanup() {
        destroyStoreIfNotNull(fileStore)
    }
}
