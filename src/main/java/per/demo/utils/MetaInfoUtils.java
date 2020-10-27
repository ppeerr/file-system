package per.demo.utils;

import org.apache.commons.lang3.StringUtils;

public class MetaInfoUtils {

    public static String getInitialMetaContent(String metaHeader, int metaBytesCount, String metaDelimiter) {
        return metaHeader + "\n" +
                StringUtils.repeat(' ', metaBytesCount) + "\n" +
                metaDelimiter + "\n";
    }

    public static boolean needToIncreaseMetaSpace(
            int metaHeaderBytesCount,
            int metaBytesCount,
            int metaContentSize,
            long currentMetaPos
    ) {
        int lastPossibleMetaPos = metaHeaderBytesCount + metaBytesCount;

        return (currentMetaPos + metaContentSize) > lastPossibleMetaPos;
    }
}
