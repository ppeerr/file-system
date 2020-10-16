/*
 * This Spock specification was generated by the Gradle 'init' task.
 */
package per.demo

import spock.lang.Specification

class FileSystemFactoryTest extends Specification {
    def "should return fileSystem when newFilwSystem called"() {
        when:
        def fileSystem = FileSystemFactory.newFileSystem()

        then:
        fileSystem
    }
}
