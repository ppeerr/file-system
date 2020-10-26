package per.demo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

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

    void putMeta(List<FileInfo> fileInfos) {
        List<FileInfo> fileInfosToUpdate = fileInfos.stream()
                .filter(Objects::nonNull)
                .filter(it -> it.getMetaInfo() != null)
                .filter(it -> it.getMetaInfo().isPresent())
                .collect(Collectors.toList());

        if (!fileInfosToUpdate.isEmpty()) {
            synchronized (updateMapLock) { //TODO check
                for (FileInfo fileInfo : fileInfosToUpdate) {
                    positionsAndSizesByNames.put(fileInfo.getName(), fileInfo.getMetaInfo());
                }
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
