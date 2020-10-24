package per.demo

import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

class CreateFromFileTest extends AbstractSpecification {

    private InFileFileSystem system

    def "should create FileSystem from existing file"() {
        given:
        def name = "FROM_FILE"
        def file = Files.createFile(Paths.get(name + EXTENSION))
        Files.writeString(file, FILE_SYSTEM_CONTENT, StandardOpenOption.WRITE)

        when:
        system = FileSystemFactory.newFileSystem(name)

        then:
        system
    }

    //TODO check validation

    void cleanup() {
        destroySystemIfNotNull(system)
    }
}
