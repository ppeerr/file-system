package per.demo.model;

import lombok.Builder;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

@Getter
@Builder
public class Configuration {

    private static final String DEFAULT_META_HEADER = "START";
    private static final String DEFAULT_META_DELIMITER = "----META ENDS----";
    private static final int DEFAULT_META_BYTES_COUNT = 2000;
    private static final int BUFFER_SIZE = 1024;

    private final String metaHeader;
    private final String metaDelimiter;
    private final int metaBytesCount;
    private final int bufferSize;

    public static Configuration defaultConfiguration() {
        return new Configuration(
                DEFAULT_META_HEADER,
                DEFAULT_META_DELIMITER,
                DEFAULT_META_BYTES_COUNT,
                BUFFER_SIZE
        );
    }

    private Configuration(String metaHeader, String metaDelimiter, int metaBytesCount, int bufferSize) {
        Validate.isTrue(StringUtils.isNotBlank(metaHeader), "metaHeader must not be blank");
        Validate.isTrue(StringUtils.isNotBlank(metaDelimiter), "metaDelimiter must not be blank");
        Validate.isTrue(metaBytesCount > 0, "metaBytesCount must be positive int");
        Validate.isTrue(bufferSize > 511, "bufferSize must be greater than 511");

        this.metaHeader = metaHeader;
        this.metaDelimiter = metaDelimiter;
        this.metaBytesCount = metaBytesCount;
        this.bufferSize = bufferSize;
    }
}
