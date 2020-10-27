package per.demo.validator

import org.apache.commons.io.IOUtils
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.charset.StandardCharsets

class UploadFileContentValidatorTest extends Specification {

    def "should pass valid fileName and content"() {
        given:
        def fileName = "test"
        def content = "content"

        when:
        UploadFileContentValidator.check(fileName, content)

        then:
        1
    }

    @Unroll
    def "should fail when invalid fileName"(String fileName) {
        given:
        def content = "content"

        when:
        UploadFileContentValidator.check(fileName, content)

        then:
        thrown(IllegalArgumentException)

        where:
        fileName << [null, "", "%spec_Symbols-"]
    }

    @Unroll
    def "should fail when invalid content"(String content) {
        given:
        def fileName = "test"

        when:
        UploadFileContentValidator.check(fileName, content)

        then:
        thrown(IllegalArgumentException)

        where:
        content << [
                null,
                "",
                IOUtils.toString(
                        getClass().classLoader.getResourceAsStream("largeContent.txt") as InputStream,
                        StandardCharsets.UTF_8.name())
        ]
    }
}
