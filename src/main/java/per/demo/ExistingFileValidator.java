package per.demo;

import org.apache.commons.lang3.Validate;

import java.util.List;

public class ExistingFileValidator {

    public static final String META_DATA_LINE_PATTERN = "(\\{\"[a-z0-9A-Z]+\",[1-9][0-9]*,[1-9][0-9]*,[A|D]})*";

    public static void checkMetaDataLines(
            List<String> metaDataLines,
            String validMetaHeader,
            int validMetaBytesCount,
            String validMetaDelimiter
    ) {
        checkMetaHeaderLine(metaDataLines.get(0), validMetaHeader);
        checkMetaInfoLine(metaDataLines.get(1), validMetaBytesCount);
        checkMetaDelimiterLine(metaDataLines.get(2), validMetaDelimiter);
    }


    private static void checkMetaHeaderLine(String metaHeader, String validHeader) {
        Validate.isTrue(validHeader.equals(metaHeader), "metaHeader must be equals " + validHeader);
    }

    private static void checkMetaInfoLine(String metaInfoLine, int validBytesCount) {
        Validate.isTrue(
                metaInfoLine.trim().matches(META_DATA_LINE_PATTERN),
                "metaInfoLine doesn't match valid pattern. E.g. {\"name\",11,12,A}"
        );

        Validate.isTrue(
                metaInfoLine.getBytes().length == validBytesCount,
                "metaBytesCount must be equals " + validBytesCount
        );
    }

    private static void checkMetaDelimiterLine(String metaDelimiter, String validDelimiter) {
        Validate.isTrue(
                validDelimiter.equals(metaDelimiter.trim()),
                "validDelimiter must be equals " + validDelimiter
        );
    }

}
