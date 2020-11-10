package per.demo.concurrent

import per.demo.AbstractSpecification
import per.demo.FileSystemFactory
import per.demo.InFileFileSystem
import spock.lang.Unroll

import java.util.concurrent.Callable
import java.util.concurrent.Executors

class InFileFileSystemCreateConcurrentTest extends AbstractSpecification {

    private InFileFileSystem systemOne

    @Unroll
    def "should successfully perform ALL creation when creation called concurrently. #i repeat"() {
        given:
        def executorService = Executors.newFixedThreadPool(5)
        systemOne = new FileSystemFactory().newFileSystem()
        def fileName = "kek1"
        List<Callable> callableList = [
                { systemOne.createFile(fileName + 1, "one") },
                { systemOne.createFile(fileName + 2, "two") },
                { systemOne.createFile(fileName + 3, "three") },
                { systemOne.createFile(fileName + 4, "four") },
                { systemOne.createFile(fileName + 5, "five") }
        ].sort { Math.random() } as List<Callable>

        when:
        def taskList = executorService.invokeAll(callableList)
        taskList.each { it.get() }

        then:
        systemOne.readFileToString(fileName + 1) == "one"
        systemOne.readFileToString(fileName + 2) == "two"
        systemOne.readFileToString(fileName + 3) == "three"
        systemOne.readFileToString(fileName + 4) == "four"
        systemOne.readFileToString(fileName + 5) == "five"

        where:
        i << (1..10)
    }

    void cleanup() {
        destroySystemIfNotNull(systemOne)
    }
}
