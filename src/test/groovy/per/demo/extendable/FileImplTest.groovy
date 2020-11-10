package per.demo.extendable

import per.demo.AbstractSpecification
import per.demo.InFileFileStore
import per.demo.model.MetaInfo

import java.nio.charset.StandardCharsets

class FileImplTest extends AbstractSpecification {

    private static final String FILE_NAME = "File.txt"
    private static final long FILE_SIZE = 10L

    private InFileFileStore store = Mock()

    void setup() {
        store.isOpen() >> true
    }

    def "should create FileImpl with valid properties"() {
        given:
        def info = new MetaInfo(5L, FILE_SIZE, 0L)

        when:
        def fileImpl = new FileImpl(FILE_NAME, store, info)

        then:
        fileImpl
        fileImpl.name == FILE_NAME
        fileImpl.size == FILE_SIZE
    }

    def "should fail when FilStore is closed"() {
        given:
        def info = new MetaInfo(5L, FILE_SIZE, 0L)

        when:
        new FileImpl(FILE_NAME, store, info)

        then:
        store.isOpen() >> false
        thrown(IllegalArgumentException)
    }

    def "should read content from File store"() {
        given:
        def startPos = 0L
        def size = 16L
        def info = new MetaInfo(startPos, size, 0L)
        def file = new FileImpl(FILE_NAME, store, info)
        store.readContentBytes(startPos, size) >> "Helloooo".getBytes(StandardCharsets.UTF_8)

        when:
        def bytes = file.read(size)

        then:
        new String(bytes, StandardCharsets.UTF_8) == "Helloooo"
    }

    def "should read all content when readBytesCount greater than file size"() {
        given:
        def startPos = 0L
        def size = 8L
        def info = new MetaInfo(startPos, size, 0L)
        def file = new FileImpl(FILE_NAME, store, info)
        store.readContentBytes(startPos, size) >> "Helloooo".getBytes(StandardCharsets.UTF_8)
        def readBytesCount = size + 1

        when:
        def bytes = file.read(readBytesCount)

        then:
        new String(bytes, StandardCharsets.UTF_8) == "Helloooo"
    }

    def "should read half when offset and readBytesCount are half from size"() {
        given:
        def startPos = 0L
        def size = 8L
        def info = new MetaInfo(startPos, size, 0L)
        def file = new FileImpl(FILE_NAME, store, info)
        def offset = 4L
        def readBytesCount = 4L
        store.readContentBytes(startPos + offset, readBytesCount) >> "oooo".getBytes(StandardCharsets.UTF_8)

        when:
        def bytes = file.read(offset, readBytesCount)

        then:
        new String(bytes, StandardCharsets.UTF_8) == "oooo"
    }

    def "should fail when offset greater than file size"() {
        given:
        def startPos = 0L
        def size = 8L
        def info = new MetaInfo(startPos, size, 0L)
        def file = new FileImpl(FILE_NAME, store, info)
        def offset = size + 1

        when:
        file.read(offset, size)

        then:
        thrown(IllegalArgumentException)
    }

    void cleanup() {
        destroyStoreIfNotNull(store)
    }
}
