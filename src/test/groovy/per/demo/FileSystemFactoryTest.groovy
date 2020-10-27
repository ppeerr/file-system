package per.demo


import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class FileSystemFactoryTest extends AbstractSpecification {

    private InFileFileSystem systemOne
    private InFileFileSystem systemTwo

    def "should create InFileFileSystem"() {
        when:
        systemOne = FileSystemFactory.newFileSystem()

        then:
        Path p = Paths.get(systemOne.getName())
        Files.exists(p)
    }

    def "should create InFileFileSystem with specific name"() {
        given:
        String name = "koko"

        when:
        systemOne = FileSystemFactory.newFileSystem(name)

        then:
        Path p = Paths.get(name + EXTENSION)
        Files.exists(p)
    }

    def "should return the same references when create FileSystems for the same files"() {
        given:
        def name = UUID.randomUUID().toString()

        when:
        systemOne = FileSystemFactory.newFileSystem(name)
        systemTwo = FileSystemFactory.newFileSystem(name)

        then:
        systemOne
        systemOne.is(systemTwo)
        Files.exists(Paths.get(name + EXTENSION))
    }

    def "should return new SystemTwo when create FileSystem for the same file after SystemOne closed"() {
        given:
        def name = UUID.randomUUID().toString()

        when:
        systemOne = FileSystemFactory.newFileSystem(name)
        systemOne.close()
        systemTwo = FileSystemFactory.newFileSystem(name)

        then:
        systemOne
        systemTwo
        !systemOne.is(systemTwo)
        !systemOne.isOpen()
        systemTwo.isOpen()
        Files.exists(Paths.get(name + EXTENSION))
    }

    void cleanup() {
        destroySystemIfNotNull(systemOne)
        destroySystemIfNotNull(systemTwo)
    }
}
