package per.demo

import per.demo.exception.FileSystemCreationException
import per.demo.model.Configuration

import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

class CreateFromFileTest extends AbstractSpecification {

    private static String NAME = "FROM_FILE"

    private InFileFileSystem system

    def "should create FileSystem from existing file"() {
        given:
        def file = Files.createFile(Paths.get(NAME + 1 + EXTENSION))
        Files.writeString(file, FILE_SYSTEM_CONTENT, StandardOpenOption.WRITE)

        when:
        system = new FileSystemFactory().newFileSystem(NAME + 1)

        then:
        system
    }

    def "should fail to create FileSystem from existing file when metaHeader is no the same"() {
        given:
        def file = Files.createFile(Paths.get(NAME + 2 + EXTENSION))
        Files.writeString(file, FILE_SYSTEM_CONTENT, StandardOpenOption.WRITE)
        def configuration = Configuration.builder()
                .metaHeader("NOT_valid")
                .metaDelimiter("----META ENDS----")
                .metaBytesCount(2000)
                .build()

        when:
        new FileSystemFactory().newFileSystem(NAME + 2, configuration)

        then:
        thrown(FileSystemCreationException)
    }

    void cleanup() {
        destroySystemIfNotNull(system)
        if (Files.exists(Paths.get(NAME + 1 + EXTENSION)))
            Files.delete(Paths.get(NAME + 1 + EXTENSION))
        if (Files.exists(Paths.get(NAME + 2 + EXTENSION)))
            Files.delete(Paths.get(NAME + 2 + EXTENSION))
    }
}
