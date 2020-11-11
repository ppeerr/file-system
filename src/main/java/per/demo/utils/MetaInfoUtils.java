package per.demo.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MetaInfoUtils {

    private static final String META_DATA_PARSE_PATTERN = "\\{([^}]+)}";

    public static String getInitialMetaContent(String metaHeader, int metaBytesCount, String metaDelimiter) {
        return metaHeader + "\n" +
                StringUtils.repeat(' ', metaBytesCount) + "\n" +
                metaDelimiter + "\n";
    }

    public static List<String> getMetaDataElements(String metaString) {
        List<String> allMatches = new ArrayList<>();
        Matcher m = Pattern
                .compile(META_DATA_PARSE_PATTERN)
                .matcher(metaString);
        while (m.find()) {
            allMatches.add(m.group(0));
        }

        return allMatches;
    }

    public static List<String> findOnlyPresentMetaElements(List<String> metaElements) {
        return metaElements.stream()
                .filter(info -> {
                    String state = info.substring(info.length() - 2, info.length() - 1);
                    return state.equals("A");
                })
                .collect(Collectors.toList());
    }
}
