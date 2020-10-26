package per.demo;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.lang3.Validate;

@Getter
@Builder
@ToString
public class Configuration {

    private static final String DEFAULT_META_HEADER = "START";
    private static final String DEFAULT_META_DELIMITER = "----META ENDS----";
    private static final int DEFAULT_META_BYTES_COUNT = 2000;

    private final String metaHeader;
    private final String metaDelimiter;
    private final int metaBytesCount;

    private Configuration(String metaHeader, String metaDelimiter, int metaBytesCount) {
        Validate.isTrue(metaHeader != null, "metaHeader must not be null");
        Validate.isTrue(metaDelimiter != null, "metaDelimiter must not be null");
        Validate.isTrue(metaBytesCount > 0, "metaBytesCount should be positive int");

        this.metaHeader = metaHeader;
        this.metaDelimiter = metaDelimiter;
        this.metaBytesCount = metaBytesCount;
    }

    public static Configuration defaultConfiguration() {
        return new Configuration(
                DEFAULT_META_HEADER,
                DEFAULT_META_DELIMITER,
                DEFAULT_META_BYTES_COUNT
        );
    }
}
