package per.demo.concurrent

import per.demo.AbstractSpecification
import per.demo.FileSystemFactory
import per.demo.InFileFileSystem
import spock.lang.Unroll

import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

class InFileFileSystemUpdateConcurrentTest extends AbstractSpecification {

    private InFileFileSystem systemOne

    def "should update valid contents when update files called from two threads"() {
        given:
        systemOne = FileSystemFactory.newFileSystem()
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
        systemOne.readFileToString("kek1") == "ch1" + CONTENT1
        systemOne.readFileToString("kek2") == CONTENT2 + "ch2"
    }

    @Unroll
    def "should ALWAYS slow task update do last when two updates called concurrently. #i repeat"() {
        given:
        def executorService = Executors.newFixedThreadPool(2)
        systemOne = FileSystemFactory.newFileSystem()
        def fileName = "kek1"

        systemOne.createFile(fileName, CONTENT1)
        List<Future> taskList = []

        when:
        taskList.add(executorService.submit({ systemOne.updateFile(fileName, "one") }))
        taskList.add(executorService.submit({ //slow task
            TimeUnit.MILLISECONDS.sleep(500)
            systemOne.updateFile(fileName, "two")
        }))

        taskList.each { it.get() }

        then:
        systemOne.contains(fileName)
        systemOne.readFileToString(fileName) == "two"

        where:
        i << (1..10)
    }

    @Unroll
    def "should successfully perform ALL updates when updates called concurrently. #i repeat"() {
        given:
        def executorService = Executors.newFixedThreadPool(5)
        systemOne = FileSystemFactory.newFileSystem()
        def fileName = "kek1"

        systemOne.createFile(fileName, CONTENT1)
        List<Callable> callableList = [
                { systemOne.updateFile(fileName, "one") },
                { systemOne.updateFile(fileName, "two") },
                { systemOne.updateFile(fileName, "three") },
                { systemOne.updateFile(fileName, "four") },
                { systemOne.updateFile(fileName, "five") }
        ].sort { Math.random() } as List<Callable>

        when:
        def taskList = executorService.invokeAll(callableList)
        taskList.each { it.get() }

        then:
        systemOne.contains(fileName)
        systemOne.readFileToString(fileName) in ["one", "two", "three", "four", "five"]

        where:
        i << (1..10)
    }

    void cleanup() {
        destroySystemIfNotNull(systemOne)
    }
}
