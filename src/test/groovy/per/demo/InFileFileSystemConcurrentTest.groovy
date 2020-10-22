package per.demo

import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class InFileFileSystemConcurrentTest extends Specification {

    private InFileFileSystem systemOne
    private InFileFileSystem systemTwo

    def "should create file 3 333"() {
        when:
        systemOne = FileSystemFactory.newFileSystem("test")
        systemTwo = FileSystemFactory.newFileSystem("test")

        then:
        systemOne
        systemOne.is(systemTwo)
        Path p = Paths.get("test" + ".iffs")
        Files.exists(p)
    }

    def "should create file  2"() {
        given:
        systemOne = FileSystemFactory.newFileSystem("test")

        when:
        def thread1 = new Thread({
            systemOne.createFile("kek1", "111111111111111111111111111111111111111111111111111111111111")
        })
        def thread2 = new Thread({
            systemOne.createFile("kek2", "222222222222222222222222222222222222222222222222222222222222")
        })

        thread1.start()
        thread2.start()

        thread1.join()
        thread2.join()
        then:
        Path p = Paths.get("test" + ".iffs")
        def lines = Files.readAllLines(p)
        !lines.isEmpty()
        println lines[0]
        println lines[1]

        println systemOne.getMap()

        systemOne.readFile("kek1") == "111111111111111111111111111111111111111111111111111111111111"
        systemOne.readFile("kek2") == "222222222222222222222222222222222222222222222222222222222222"
    }

    void cleanup() {
        if (systemOne != null)
            FileSystemFactory.destroy(systemOne.getName())
        if (systemTwo != null)
            FileSystemFactory.destroy(systemTwo.getName())
    }
}
