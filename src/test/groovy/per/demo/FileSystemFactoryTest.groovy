package per.demo

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class FileSystemFactoryTest extends AbstractSpecification {

    private InFileFileSystem system

    def "should create InFIleFileSystem"() {
        when:
        system = FileSystemFactory.newFileSystem()

        then:
        Path p = Paths.get(system.getName())
        Files.exists(p)
    }

    def "should create InFIleFileSystem with specific name"() {
        given:
        String name = "koko"

        when:
        system = FileSystemFactory.newFileSystem(name)

        then:
        Path p = Paths.get(name + EXTENSION)
        Files.exists(p)
    }

    void cleanup() {
        destroySystemIfNotNull(system)
    }
}
