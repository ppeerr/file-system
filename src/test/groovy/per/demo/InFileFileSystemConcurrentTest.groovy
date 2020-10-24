package per.demo

import java.nio.file.Files
import java.nio.file.Paths

class InFileFileSystemConcurrentTest extends AbstractSpecification {

    private static final String CONTENT1 = "111111111111111111111111111111111111111111111111111111111111"
    private static final String CONTENT2 = "222222222222222222222222222222222222222222222222222222222222"

    private InFileFileSystem systemOne
    private InFileFileSystem systemTwo

    def "should return the same references when create FileSystems for the same files"() {
        given:
        def name = "test"

        when:
        systemOne = FileSystemFactory.newFileSystem(name)
        systemTwo = FileSystemFactory.newFileSystem(name)

        then:
        systemOne
        systemOne.is(systemTwo)
        Files.exists(Paths.get(name + EXTENSION))
    }

    def "should create be able to create files when called from two threads"() {
        given:
        def name = "test"
        systemOne = FileSystemFactory.newFileSystem(name)

        when:
        def thread1 = new Thread({ systemOne.createFile("kek1", CONTENT1) })
        def thread2 = new Thread({ systemOne.createFile("kek2", CONTENT2) })

        thread1.start()
        thread2.start()

        thread1.join()
        thread2.join()

        then:
        def lines = Files.readAllLines(Paths.get(name + EXTENSION))
        !lines.isEmpty()
        lines.contains(CONTENT1)
        lines.contains(CONTENT2)
    }

    def "should write valid contents when create files called from two threads"() {
        given:
        def name = "test"
        systemOne = FileSystemFactory.newFileSystem(name)

        when:
        def thread1 = new Thread({ systemOne.createFile("kek1", CONTENT1) })
        def thread2 = new Thread({ systemOne.createFile("kek2", CONTENT2) })

        thread1.start()
        thread2.start()

        thread1.join()
        thread2.join()

        then:
        systemOne.readFile("kek1") == CONTENT1
        systemOne.readFile("kek2") == CONTENT2
    }

    def "should update valid contents when update files called from two threads"() {
        given:
        def name = "test"
        systemOne = FileSystemFactory.newFileSystem(name)
        systemOne.createFile("kek1", CONTENT1)
        systemOne.createFile("kek2", CONTENT2)

        when:
        def thread1 = new Thread({ systemOne.updateFile("kek1", "ch1" + CONTENT1) })
        def thread2 = new Thread({ systemOne.updateFile("kek2", CONTENT2 + "ch2") })

        thread1.start()
        thread2.start()

        thread1.join()
        thread2.join()

        then:
        systemOne.readFile("kek1") == "ch1" + CONTENT1
        systemOne.readFile("kek2") == CONTENT2 + "ch2"
    }

    def "should delete files when delete called from two threads"() {
        given:
        def name = "test"
        systemOne = FileSystemFactory.newFileSystem(name)
        systemOne.createFile("kek1", CONTENT1)
        systemOne.createFile("kek2", CONTENT2)
        systemOne.createFile("kek3", CONTENT1)

        when:
        def thread1 = new Thread({ systemOne.deleteFile("kek3") })
        def thread2 = new Thread({ systemOne.deleteFile("kek1") })

        thread1.start()
        thread2.start()

        thread1.join()
        thread2.join()

        then:
        def names = systemOne.allFileNames()
        names.size() == 1
        names.contains("kek2")
        systemOne.readFile("kek2") == CONTENT2
    }

    def "should read contents when read called from two threads"() {
        given:
        def name = "test"
        systemOne = FileSystemFactory.newFileSystem(name)
        systemOne.createFile("kek1", CONTENT1)
        systemOne.createFile("kek2", CONTENT2)

        when:
        String content2 = ""
        String content1 = ""
        def thread1 = new Thread({ content2 = systemOne.readFile("kek2") })
        def thread2 = new Thread({ content1 = systemOne.readFile("kek1") })

        thread1.start()
        thread2.start()

        thread1.join()
        thread2.join()

        then:
        content1 == CONTENT1
        content2 == CONTENT2
    }

    void cleanup() {
        destroySystemIfNotNull(systemOne)
        destroySystemIfNotNull(systemTwo)
    }
}
