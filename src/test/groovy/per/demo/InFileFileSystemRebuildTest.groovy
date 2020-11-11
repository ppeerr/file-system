package per.demo

import per.demo.model.Configuration

import java.nio.channels.Channels

class InFileFileSystemRebuildTest extends AbstractSpecification {

    private static final NAME = "kekek_rebuild"

    private InFileFileSystem system

    def "should create THREE files and rebuild metaspace ONCE"() {
        given:
        system = new FileSystemFactory().newFileSystem(NAME, Configuration.builder()
                .metaHeader("START")
                .metaDelimiter("--END--")
                .metaBytesCount(25)
                .build())
        def name = "kek"

        when:
        system.createFile(name, "Hello_world")
        system.createFile(name + "2", "Hello_world and You!2")
        system.createFile(name + "3", "Hello_world and You!3")

        then:
        def fileNames = system.allFileNames()
        fileNames.size() == 3
        system.readFileToString(name) == "Hello_world"
        system.readFileToString(name + "2") == "Hello_world and You!2"
        system.readFileToString(name + "3") == "Hello_world and You!3"
    }

    def "should create THREE files and rebuild metaspace ONCE. InputStream"() {
        given:
        system = new FileSystemFactory().newFileSystem(NAME, Configuration.builder()
                .metaHeader("START")
                .metaDelimiter("--END--")
                .metaBytesCount(25)
                .build())
        def name = "kek"

        when:
        system.createFile(name, new ByteArrayInputStream("Hello_world".getBytes()))
        system.createFile(name + "2", new ByteArrayInputStream("Hello_world and You!2".getBytes()))
        system.createFile(name + "3", new ByteArrayInputStream("Hello_world and You!3".getBytes()))

        then:
        def fileNames = system.allFileNames()
        fileNames.size() == 3
        system.readFileToString(name) == "Hello_world"
        system.readFileToString(name + "2") == "Hello_world and You!2"
        system.readFileToString(name + "3") == "Hello_world and You!3"
    }

    def "should create THREE files and rebuild metaspace ONCE. Channel"() {
        given:
        system = new FileSystemFactory().newFileSystem(NAME, Configuration.builder()
                .metaHeader("START")
                .metaDelimiter("--END--")
                .metaBytesCount(25)
                .build())
        def name = "kek"

        when:
        system.createFile(name, Channels.newChannel(new ByteArrayInputStream("Hello_world".getBytes())))
        system.createFile(name + "2", Channels.newChannel(new ByteArrayInputStream("Hello_world and You!2".getBytes())))
        system.createFile(name + "3", Channels.newChannel(new ByteArrayInputStream("Hello_world and You!3".getBytes())))

        then:
        def fileNames = system.allFileNames()
        fileNames.size() == 3
        system.readFileToString(name) == "Hello_world"
        system.readFileToString(name + "2") == "Hello_world and You!2"
        system.readFileToString(name + "3") == "Hello_world and You!3"
    }

    def "should create FOUR files and rebuild metaspace TWO times"() {
        given:
        system = new FileSystemFactory().newFileSystem(NAME, Configuration.builder()
                .metaHeader("START")
                .metaDelimiter("--END--")
                .metaBytesCount(25)
                .build())
        def name = "kek"

        when:
        system.createFile(name, "Hello_world")
        system.createFile(name + "2", "Hello_world and You!2")
        system.createFile(name + "3", "Hello_world and You!3")
        system.createFile(name + "4", "Hello_world and You!4")

        then:
        def fileNames = system.allFileNames()
        fileNames.size() == 4
        system.readFileToString(name) == "Hello_world"
        system.readFileToString(name + "2") == "Hello_world and You!2"
        system.readFileToString(name + "3") == "Hello_world and You!3"
        system.readFileToString(name + "4") == "Hello_world and You!4"
    }

    def "should create FOUR files and rebuild metaspace TWO times. InputStream"() {
        given:
        system = new FileSystemFactory().newFileSystem(NAME, Configuration.builder()
                .metaHeader("START")
                .metaDelimiter("--END--")
                .metaBytesCount(25)
                .build())
        def name = "kek"

        when:
        system.createFile(name, new ByteArrayInputStream("Hello_world".getBytes()))
        system.createFile(name + "2", new ByteArrayInputStream("Hello_world and You!2".getBytes()))
        system.createFile(name + "3", new ByteArrayInputStream("Hello_world and You!3".getBytes()))
        system.createFile(name + "4", new ByteArrayInputStream("Hello_world and You!4".getBytes()))

        then:
        def fileNames = system.allFileNames()
        fileNames.size() == 4
        system.readFileToString(name) == "Hello_world"
        system.readFileToString(name + "2") == "Hello_world and You!2"
        system.readFileToString(name + "3") == "Hello_world and You!3"
        system.readFileToString(name + "4") == "Hello_world and You!4"
    }

    def "should create FOUR files and rebuild metaspace TWO times. Channel"() {
        given:
        system = new FileSystemFactory().newFileSystem(NAME, Configuration.builder()
                .metaHeader("START")
                .metaDelimiter("--END--")
                .metaBytesCount(25)
                .build())
        def name = "kek"

        when:
        system.createFile(name, Channels.newChannel(new ByteArrayInputStream("Hello_world".getBytes())))
        system.createFile(name + "2", Channels.newChannel(new ByteArrayInputStream("Hello_world and You!2".getBytes())))
        system.createFile(name + "3", Channels.newChannel(new ByteArrayInputStream("Hello_world and You!3".getBytes())))
        system.createFile(name + "4", Channels.newChannel(new ByteArrayInputStream("Hello_world and You!4".getBytes())))

        then:
        def fileNames = system.allFileNames()
        fileNames.size() == 4
        system.readFileToString(name) == "Hello_world"
        system.readFileToString(name + "2") == "Hello_world and You!2"
        system.readFileToString(name + "3") == "Hello_world and You!3"
        system.readFileToString(name + "4") == "Hello_world and You!4"
    }

    def "should create TWO files and rebuild metaspace TWO times for the 2nd file. Channel"() {
        given:
        system = new FileSystemFactory().newFileSystem(NAME, Configuration.builder()
                .metaHeader("START")
                .metaDelimiter("--END--")
                .metaBytesCount(17)
                .build())
        def name = "kek"

        when:
        system.createFile(name, Channels.newChannel(new ByteArrayInputStream("Hello_world".getBytes())))
        system.createFile(name + "1234567", Channels.newChannel(new ByteArrayInputStream("Hello_world and You!2".getBytes())))

        then:
        def fileNames = system.allFileNames()
        fileNames.size() == 2
        system.readFileToString(name) == "Hello_world"
        system.readFileToString(name + "1234567") == "Hello_world and You!2"
    }

    void cleanup() {
        destroySystemIfNotNull(system)
    }
}
