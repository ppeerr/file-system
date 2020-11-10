package per.demo

import per.demo.model.Configuration

import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

class InFileFileStoreImplTest extends AbstractSpecification {

    private InFileFileStoreImpl fileStore

    def "should create FileSystem with valid from existent file"() {
        given:
        def name = "FROM_FILE_store"
        def file = Files.createFile(Paths.get(name + EXTENSION))
        Files.writeString(file, FILE_SYSTEM_CONTENT, StandardOpenOption.WRITE)

        when:
        fileStore = new InFileFileStoreImpl(name + EXTENSION, Configuration.defaultConfiguration())

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
                .build()
        fileStore = new InFileFileStoreImpl(name + EXTENSION, smallMetaSpaceConfiguration)

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
