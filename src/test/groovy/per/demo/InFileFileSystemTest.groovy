package per.demo

import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.regex.Matcher
import java.util.regex.Pattern

class InFileFileSystemTest extends Specification {

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
        system.createFile(name + "1", "Hello_world3")

        then:
        Path p = Paths.get(NAME + ".iffs")
        def string = Files.readString(p)
        def lines = Files.readAllLines(p)
        !lines.isEmpty()
//        lines.get(0) == "Hello_world"
    }

    def "asda"() {
        when:
//        def split = " {\"kek\",2026,11,A};{\"kek1\",2037,12,A} ".split(".*\\{([^)]+)}.*")

        List<String> allMatches = new ArrayList<>();
        Matcher m = Pattern
                .compile("\\{([^}]+)}")
                .matcher(" {\"kek\",2026,11,A};{\"kek1\",2037,12,A} ");
        while (m.find()) {
            allMatches.add(m.group(1));
        }

        allMatches.stream()
                .collect { it.split(",") }
        .forEach {
            println "name=" + it[0]
            println "start=" + it[1]
            println "size=" + it[2]
            println "state=" + it[3]
        }
        then:
        allMatches.size() == 2
    }

    def "should create 2 files"() {
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
        Path p = Paths.get(NAME + ".iffs")
        def lines = Files.readAllLines(p)
        !lines.isEmpty()
//        lines.get(0) == "Hello_world"
//        lines.get(1) == "Hello_world and You!"
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
//        lines.size() == 2
//        lines.get(0) == "Hello_world"
//        lines.get(1) == "Hello_world and You!"

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
//        lines.size() == 3
//        lines.get(0) == "Hello_world"
//        lines.get(1) == "Hello_world and You!"
//        lines.get(2) == "hooray!"

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

    def "should fail when try to read non-existent file"() {
        given:
        def name = "kek"
        system.createFile(name, "Hello_world")

        when:
        system.readFile(name + "new")

        then:
        thrown(RuntimeException)
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
