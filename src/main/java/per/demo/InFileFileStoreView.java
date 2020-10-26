package per.demo;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class InFileFileStoreView {

    private ConcurrentMap<String, MetaInfo> positionsAndSizesByNames = new ConcurrentHashMap<>();
    private static final Object updateMapLock = new Object();

    boolean contains(String fileName) {
        return positionsAndSizesByNames.containsKey(fileName);
    }

    MetaInfo getMeta(String fileName) {
        MetaInfo metaInfo = positionsAndSizesByNames.get(fileName);

        return metaInfo != null
                ? new MetaInfo(metaInfo)
                : null;
    }

    void putMeta(String fileName, MetaInfo metaInfo) {
        if (metaInfo != null && metaInfo.isPresent()) {
            synchronized (updateMapLock) { //TODO check
                positionsAndSizesByNames.put(fileName, metaInfo);
            }
        }
    }

    void remove(String fileName) {
        MetaInfo metaInfo = positionsAndSizesByNames.get(fileName);

        if (metaInfo != null && metaInfo.isPresent()) {
            synchronized (updateMapLock) { //TODO check
                positionsAndSizesByNames.remove(fileName);
            }
        }
    }

    Map<String, MetaInfo> getMap() {
        return new HashMap<>(positionsAndSizesByNames);
    }
}
