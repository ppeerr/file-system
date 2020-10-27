package per.demo.model

import spock.lang.Specification
import spock.lang.Unroll

class ConfigurationTest extends Specification {

    def "should successfully create Configuration"() {
        when:
        def configuration = Configuration.builder()
                .metaHeader("header")
                .metaDelimiter("delimiter")
                .metaBytesCount(30)
                .build()

        then:
        configuration
    }

    @Unroll
    def "should fail to create Configuration when header is null or blank"(String metaHeader) {
        when:
        Configuration.builder()
                .metaHeader(metaHeader)
                .metaDelimiter("delimiter")
                .metaBytesCount(30)
                .build()

        then:
        thrown(IllegalArgumentException)

        where:
        metaHeader << [null, ""]
    }

    @Unroll
    def "should fail to create Configuration when metaDelimiter is null or blank"(String metaDelimiter) {
        when:
        Configuration.builder()
                .metaHeader("header")
                .metaDelimiter(metaDelimiter)
                .metaBytesCount(30)
                .build()

        then:
        thrown(IllegalArgumentException)

        where:
        metaDelimiter << [null, ""]
    }

    @Unroll
    def "should fail to create Configuration when metaBytesCount is zero or negative"(int metaBytesCount, int testNumber) {
        when:
        Configuration.builder()
                .metaHeader("header")
                .metaDelimiter("delimiter")
                .metaBytesCount(metaBytesCount)
                .build()

        then:
        thrown(IllegalArgumentException)

        where:
        metaBytesCount | testNumber
        0              | 1
        -1             | 2
    }
}
