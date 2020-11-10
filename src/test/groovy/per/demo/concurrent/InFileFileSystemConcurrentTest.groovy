package per.demo.concurrent

import per.demo.AbstractSpecification
import per.demo.FileSystemFactory
import per.demo.InFileFileSystem

class InFileFileSystemConcurrentTest extends AbstractSpecification {

    private InFileFileSystem systemOne
    private InFileFileSystem systemTwo

    def "should delete files when delete called from two threads"() {
        given:
        systemOne = new FileSystemFactory().newFileSystem()
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
        systemOne.readFileToString("kek2") == CONTENT2
    }

    def "should read contents when read called from two threads"() {
        given:
        systemOne = new FileSystemFactory().newFileSystem()
        systemOne.createFile("kek1", CONTENT1)
        systemOne.createFile("kek2", CONTENT2)

        when:
        String content2 = ""
        String content1 = ""
        def thread1 = new Thread({ content2 = systemOne.readFileToString("kek2") })
        def thread2 = new Thread({ content1 = systemOne.readFileToString("kek1") })

        thread1.start()
        thread2.start()

        thread1.join()
        thread2.join()

        then:
        content1 == CONTENT1
        content2 == CONTENT2
    }

    void cleanup() {
        destroySystemIfNotNull(systemOne, systemTwo)
    }
}
