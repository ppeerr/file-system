package per.demo.validator

import per.demo.AbstractSpecification

class ExistingFileValidatorTest extends AbstractSpecification {

    private static final String VALID_HEADER = "START"
    private static final int VALID_META_BYTES_COUNT = 2000
    private static final String VALID_DELIMITER = "----META ENDS----"

    def "should pass valid MetaData"() {
        given:
        def validFileData = FILE_SYSTEM_CONTENT.split("\n")
        def metaDataLines = [
                validFileData[0], validFileData[1], validFileData[2]
        ]

        when:
        ExistingFileValidator.checkMetaDataLines(
                metaDataLines,
                VALID_HEADER,
                VALID_META_BYTES_COUNT,
                VALID_DELIMITER
        )

        then:
        1
    }

    def "should fail when metaHeader is not the same"() {
        given:
        def validFileData = FILE_SYSTEM_CONTENT.split("\n")
        def metaDataLines = [
                "invalid", validFileData[1], validFileData[2]
        ]

        when:
        ExistingFileValidator.checkMetaDataLines(
                metaDataLines,
                VALID_HEADER,
                VALID_META_BYTES_COUNT,
                VALID_DELIMITER
        )

        then:
        thrown(IllegalArgumentException)
    }

    def "should fail when metaDelimiter is not the same"() {
        given:
        def validFileData = FILE_SYSTEM_CONTENT.split("\n")
        def metaDataLines = [
                validFileData[0], validFileData[1], "invalid"
        ]

        when:
        ExistingFileValidator.checkMetaDataLines(
                metaDataLines,
                VALID_HEADER,
                VALID_META_BYTES_COUNT,
                VALID_DELIMITER
        )

        then:
        thrown(IllegalArgumentException)
    }

    def "should fail when metaBytesCount is not the same"() {
        given:
        def validFileData = FILE_SYSTEM_CONTENT.split("\n")
        def metaDataLines = [
                validFileData[0], validFileData[1].trim(), validFileData[2]
        ]

        when:
        ExistingFileValidator.checkMetaDataLines(
                metaDataLines,
                VALID_HEADER,
                VALID_META_BYTES_COUNT,
                VALID_DELIMITER
        )

        then:
        thrown(IllegalArgumentException)
    }

    def "should fail when metaLine pattern is not valid"() {
        given:
        def validFileData = FILE_SYSTEM_CONTENT.split("\n")
        def metaDataLines = [
                validFileData[0], "{\"n-ame\",45,0,A}", validFileData[2]
        ]

        when:
        ExistingFileValidator.checkMetaDataLines(
                metaDataLines,
                VALID_HEADER,
                VALID_META_BYTES_COUNT,
                VALID_DELIMITER
        )

        then:
        thrown(IllegalArgumentException)
    }
}
