package per.demo

import per.demo.model.Configuration

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

    def "should create FOUR files and rebuild metaspace TWO time"() {
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

    void cleanup() {
        destroySystemIfNotNull(system)
    }
}
