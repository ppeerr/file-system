package per.demo;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@Builder
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class Configuration {

    private static final String DEFAULT_META_HEADER = "START";
    private static final String DEFAULT_META_DELIMITER = "----META ENDS----";
    private static final int DEFAULT_META_BYTES_COUNT = 2000;

    private final String metaHeader;
    private final String metaDelimiter;
    private final int metaBytesCount;

    public static Configuration defaultConfiguration() {
        return new Configuration(
                DEFAULT_META_HEADER,
                DEFAULT_META_DELIMITER,
                DEFAULT_META_BYTES_COUNT
        );
    }
}
