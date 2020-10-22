package per.demo

import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class InFileFileSystemTest extends Specification {

    private static final NAME = "kekek"

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
        Path p = Paths.get(NAME + ".iffs")
        def lines = Files.readAllLines(p)
        !lines.isEmpty()
        lines.get(0) == "Hello_world"
    }

    def "should create 2 files"() {
        given:
        def name = "kek"

        when:
        system.createFile(name, "Hello_world")
        system.createFile(name + "2", "Hello_world and You!")

        then:
        Path p = Paths.get(NAME + ".iffs")
        def lines = Files.readAllLines(p)
        !lines.isEmpty()
        lines.get(0) == "Hello_world"
        lines.get(1) == "Hello_world and You!"
    }

    def "should delete one file"() {
        given:
        def name = "kek"
        system.createFile(name, "Hello_world")
        system.createFile(name + "2", "Hello_world and You!")

        when:
        system.deleteFile(name)

        then:
        Path p = Paths.get(NAME + ".iffs")
        def lines = Files.readAllLines(p)
        lines.size() == 2
        lines.get(0) == "Hello_world"
        lines.get(1) == "Hello_world and You!"

        def names = system.allFileNames()
        names.size() == 1
        names[0] == name + "2"
    }

    def "should update file"() {
        given:
        def name = "kek"
        system.createFile(name, "Hello_world")
        system.createFile(name + "2", "Hello_world and You!")

        when:
        system.updateFile(name, "hooray!")

        then:
        Path p = Paths.get(NAME + ".iffs")
        def lines = Files.readAllLines(p)
        lines.size() == 3
        lines.get(0) == "Hello_world"
        lines.get(1) == "Hello_world and You!"
        lines.get(2) == "hooray!"

        def names = system.allFileNames()
        names.size() == 2
        names.contains(name + "2")
        names.contains(name)
    }

    def "should read file content"() {
        given:
        def name = "kek"
        system.createFile(name, "Hello_world")
        system.createFile(name + "2", "Hello_world and You!")

        when:
        def file = system.readFile(name)
        def file2 = system.readFile(name + "2")

        then:
        file == "Hello_world"
        file2 == "Hello_world and You!"
    }

    def "should update file and then read content"() {
        given:
        def name = "kek"
        system.createFile(name, "Hello_world")
        system.createFile(name + "2", "Hello_world and You!")

        when:
        system.updateFile(name, "Changed")
        def file = system.readFile(name)
        def file2 = system.readFile(name + "2")

        then:
        file == "Changed"
        file2 == "Hello_world and You!"
    }

    void cleanup() {
        FileSystemFactory.destroy(system.getName())
    }
}