package per.demo

import per.demo.exception.ReadFileException

import java.nio.channels.Channels
import java.nio.channels.ReadableByteChannel
import java.nio.charset.StandardCharsets

class InFileFileSystemTest extends AbstractSpecification {

    private static final NAME = "kekek2"

    private InFileFileSystem system

    void setup() {
        system = new FileSystemFactory().newFileSystem(NAME)
    }

    def "should create file"() {
        given:
        def name = "kek"

        when:
        system.createFile(name, "Hello_world")

        then:
        def fileNames = system.allFileNames()
        !fileNames.isEmpty()
        fileNames.contains(name)
    }

    def "should create 8 files"() {
        given:
        def name = "kek"

        when:
        system.createFile(name, "Hello_world")
        system.createFile(name + "2", "Hello_world and You!2")
        system.createFile(name + "3", "Hello_world and You!3")
        system.createFile(name + "4", "Hello_world and You!4")
        system.createFile(name + "5", "Hello_world and You!5")
        system.createFile(name + "6", "Hello_world and You!6")
        system.createFile(name + "7", "Hello_world and You!7")
        system.createFile(name + "8", "Hello_world and You!8")

        then:
        def fileNames = system.allFileNames()
        fileNames.size() == 8
    }

    def "should create 1 file from InputStream"() {
        given:
        def name = "kek"
        InputStream targetStream = new ByteArrayInputStream("Hello_world".getBytes())

        when:
        system.createFile(name, targetStream)

        then:
        def fileNames = system.allFileNames()
        fileNames.size() == 1
        system.readFileToString(name) == "Hello_world"
    }

    def "should create 1 file from channel"() {
        given:
        def name = "kek"
        InputStream targetStream = new ByteArrayInputStream("Hello_world".getBytes())
        ReadableByteChannel channel = Channels.newChannel(targetStream)

        when:
        system.createFile(name, channel)

        then:
        def fileNames = system.allFileNames()
        fileNames.size() == 1
        system.readFileToString(name) == "Hello_world"
    }

    def "should delete one file"() {
        given:
        def name = "kek"
        system.createFile(name, "Hello_world")
        system.createFile(name + "2", "Hello_world and You!")

        when:
        system.deleteFile(name)

        then:
        def fileNames = system.allFileNames()
        fileNames.size() == 1
        !fileNames.contains(name)
    }

    def "should read file content"() {
        given:
        def name = "kek"
        system.createFile(name, "Hello_world")
        system.createFile(name + "2", "Hello_world and You!")

        when:
        def file = system.readFileToString(name)
        def file2 = system.readFileToString(name + "2")

        then:
        file == "Hello_world"
        file2 == "Hello_world and You!"
    }


    def "should update file"() {
        given:
        def name = "kek"
        system.createFile(name, "Hello_world")
        system.createFile(name + "2", "Hello_world and You!")

        when:
        system.updateFile(name, "hooray!")

        then:
        def fileNames = system.allFileNames()
        fileNames.size() == 2
        system.readFileToString(name) == "hooray!"
    }

    def "should update file via inputStream"() {
        given:
        def name = "kek"
        system.createFile(name, "Hello_world")
        system.createFile(name + "2", "Hello_world and You!")
        InputStream targetStream = new ByteArrayInputStream("hooray!".getBytes())

        when:
        system.updateFile(name, targetStream)

        then:
        def fileNames = system.allFileNames()
        fileNames.size() == 2
        system.readFileToString(name) == "hooray!"
    }

    def "should update file via Channel"() {
        given:
        def name = "kek"
        system.createFile(name, "Hello_world")
        system.createFile(name + "2", "Hello_world and You!")
        InputStream targetStream = new ByteArrayInputStream("hooray!".getBytes())
        ReadableByteChannel channel = Channels.newChannel(targetStream)

        when:
        system.updateFile(name, channel)

        then:
        def fileNames = system.allFileNames()
        fileNames.size() == 2
        system.readFileToString(name) == "hooray!"
    }

    def "should fail when try to read non-existent file"() {
        given:
        def name = "kek"
        system.createFile(name, "Hello_world")

        when:
        system.readFileToString(name + "new")

        then:
        thrown(ReadFileException)
    }

    def "should update file and then read content"() {
        given:
        def name = "kek"
        system.createFile(name, "Hello_world")
        system.createFile(name + "2", "Hello_world and You!")

        when:
        system.updateFile(name, "Changed")
        def file = system.readFileToString(name)
        def file2 = system.readFileToString(name + "2")

        then:
        file == "Changed"
        file2 == "Hello_world and You!"
    }

    def "should return File and then read content"() {
        given:
        def name = "kek"
        system.createFile(name, "Hello_world")
        system.createFile(name + "2", "Hello_world and You!")

        when:
        def file = system.getFile(name)

        then:
        file
        file.getName() == name
    }

    def "should can read content from File"() {
        given:
        def name = "kek"
        system.createFile(name, "Hello_world")
        system.createFile(name + "2", "Hello_world and You!")

        when:
        def bytes = system.getFile(name).getBytes()

        then:
        new String(bytes, StandardCharsets.UTF_8) == "Hello_world"
    }

    def "should can read part of content from File"() {
        given:
        def name = "kek"
        system.createFile(name, "Hello_world")
        def neededByteCount = 5L
        def file = system.getFile(name)

        when:
        def bytes = file.read(neededByteCount)

        then:
        new String(bytes, StandardCharsets.UTF_8) == "Hello"
    }

    def "should can read any part of content from File"() {
        given:
        def name = "kek"
        system.createFile(name, "Hello_world")
        def neededByteCount = 5L
        def offset = 6L
        def file = system.getFile(name)

        when:
        def bytes = file.read(offset, neededByteCount)

        then:
        new String(bytes, StandardCharsets.UTF_8) == "world"
    }

    def "should can read content by bytes from File"() {
        given:
        def name = "kek"
        system.createFile(name, "Hello_world")
        def file = system.getFile(name)

        when:
        def result = ""
        for (int i = 0; i < file.getSize(); i++) {
            def offset = i
            def bytes = file.read(offset, 1L)

            result += new String(bytes, StandardCharsets.UTF_8)
        }

        then:
        result == "Hello_world"
    }

    void cleanup() {
        destroySystemIfNotNull(system)
    }
}
