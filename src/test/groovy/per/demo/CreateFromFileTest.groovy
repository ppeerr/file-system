package per.demo


import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

class CreateFromFileTest extends Specification {

    private InFileFileSystem system

    def "should create from existent file"() {
        given:
        def name = "FROM_FILE"
        Path p = Paths.get(name+".iffs"); //needed?
        def file = Files.createFile(p);
        Files.writeString(
                file,
                "START\n" +
                        "{\"kek\",2025,11,A};{\"kek1\",2037,12,A}                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            \n" +
                        "----META ENDS----\n" +
                        "Hello_world\n" +
                        "Hello_world3",
                StandardOpenOption.WRITE
        )

        when:
        system = FileSystemFactory.newFileSystem(name)
        def map = system.getMap()

        then:
        map.size() == 2
    }

    void cleanup() {
        if (system != null)
            FileSystemFactory.destroy(system.getName())
    }
}
