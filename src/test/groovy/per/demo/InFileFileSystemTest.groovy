package per.demo

import per.demo.exception.ReadFileException

class InFileFileSystemTest extends AbstractSpecification {

    private static final NAME = "kekek2"

    private InFileFileSystem system

    void setup() {
        system = FileSystemFactory.newFileSystem(NAME)
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

    void cleanup() {
        destroySystemIfNotNull(system)
    }
}
