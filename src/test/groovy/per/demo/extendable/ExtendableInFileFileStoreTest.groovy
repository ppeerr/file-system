package per.demo.extendable

import per.demo.AbstractSpecification
import per.demo.model.Configuration

import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

class ExtendableInFileFileStoreTest extends AbstractSpecification {

    private ExtendableInFileFileStore fileStore

    def "should create FileSystem with valid from existent file"() {
        given:
        def name = "FROM_FILE_store"
        def file = Files.createFile(Paths.get(name + EXTENSION))
        Files.writeString(file, FILE_SYSTEM_CONTENT, StandardOpenOption.WRITE)

        when:
        fileStore = new ExtendableInFileFileStore(Paths.get(name + EXTENSION), Configuration.defaultConfiguration())

        then:
        fileStore
        fileStore.getMetaContent().trim() == "{\"kek\",2025,11,A}{\"kek1\",2037,12,A}"
    }

    def "should be able to increase meta space many times"() {
        given:
        def name = "FROM_FILE_rebuild"

        def smallMetaSpaceConfiguration = Configuration.builder()
                .metaHeader("START")
                .metaDelimiter("--END--")
                .metaBytesCount(5)
                .bufferSize(512)
                .build()
        fileStore = new ExtendableInFileFileStore(Paths.get(name + EXTENSION), smallMetaSpaceConfiguration)

        when:
        fileStore.saveContent("kek1", "any")

        then:
        fileStore
        fileStore.getMetaContent().trim() == "{\"kek1\",35,3,A}"
    }

    void cleanup() {
        destroyStoreIfNotNull(fileStore)
    }
}
