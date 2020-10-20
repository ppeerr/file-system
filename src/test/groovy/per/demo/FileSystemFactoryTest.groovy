package per.demo

import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class FileSystemFactoryTest extends Specification {

    private InFileFileSystem system

    def "should create InFIleFileSystem"() {
        when:
        system = FileSystemFactory.newFileSystem()

        then:
        Path p = Paths.get(system.getFileSystemName() + ".txt")
        Files.exists(p)
    }

    def "should create InFIleFileSystem with specific name"() {
        given:
        String name = "koko"

        when:
        system = FileSystemFactory.newFileSystem(name)

        then:
        Path p = Paths.get(name + ".txt")
        Files.exists(p)
    }

    void cleanup() {
        if (system != null)
            system.destroy()
    }
}
