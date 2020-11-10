package per.demo.concurrent

import per.demo.AbstractSpecification
import per.demo.FileSystemFactory
import per.demo.InFileFileSystem

import java.nio.file.Files
import java.nio.file.Paths

class InFileFileSystemBaseConcurrentTest extends AbstractSpecification {

    private InFileFileSystem systemOne
    private InFileFileSystem systemTwo

    def "should be able to create files when called from two threads"() {
        given:
        systemOne = new FileSystemFactory().newFileSystem()

        when:
        def thread1 = new Thread({ systemOne.createFile("kek1", CONTENT1) })
        def thread2 = new Thread({ systemOne.createFile("kek2", CONTENT2) })

        thread1.start()
        thread2.start()

        thread1.join()
        thread2.join()

        then:
        def lines = Files.readAllLines(Paths.get(systemOne.getName()))
        !lines.isEmpty()
        lines.contains(CONTENT1)
        lines.contains(CONTENT2)
    }

    def "should write valid contents when create files called from two threads"() {
        given:
        systemOne = new FileSystemFactory().newFileSystem()

        when:
        def thread1 = new Thread({ systemOne.createFile("kek1", CONTENT1) })
        def thread2 = new Thread({ systemOne.createFile("kek2", CONTENT2) })

        thread1.start()
        thread2.start()

        thread1.join()
        thread2.join()

        then:
        systemOne.readFileToString("kek1") == CONTENT1
        systemOne.readFileToString("kek2") == CONTENT2
    }

    void cleanup() {
        destroySystemIfNotNull(systemOne, systemTwo)
    }
}
