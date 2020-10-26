package per.demo

import spock.lang.Specification

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

    def "should fail to create Configuration when header is null"() {
        when:
        Configuration.builder()
                .metaDelimiter("delimiter")
                .metaBytesCount(30)
                .build()

        then:
        thrown(IllegalArgumentException)
    }

    def "should fail to create Configuration when metaDelimiter is null"() {
        when:
        Configuration.builder()
                .metaHeader("header")
                .metaBytesCount(30)
                .build()

        then:
        thrown(IllegalArgumentException)
    }

    def "should fail to create Configuration when metaBytesCount is zero"() {
        when:
        Configuration.builder()
                .metaHeader("header")
                .metaDelimiter("delimiter")
                .build()

        then:
        thrown(IllegalArgumentException)
    }

    def "should fail to create Configuration when metaBytesCount is negative"() {
        when:
        Configuration.builder()
                .metaHeader("header")
                .metaDelimiter("delimiter")
                .metaBytesCount(-1)
                .build()

        then:
        thrown(IllegalArgumentException)
    }
}
