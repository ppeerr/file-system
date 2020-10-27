package per.demo;

import per.demo.model.FileInfo;
import per.demo.model.MetaInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

public class InFileFileStoreView {

    private ConcurrentMap<String, MetaInfo> positionsAndSizesByNames = new ConcurrentHashMap<>();
    private final Object updateMapLock = new Object();

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

        if (fileInfosToUpdate.isEmpty()) {
            return;
        }

        synchronized (updateMapLock) {
            for (FileInfo fileInfo : fileInfosToUpdate) {
                positionsAndSizesByNames.put(fileInfo.getName(), fileInfo.getMetaInfo());
            }
        }
    }

    void remove(String fileName) {
        MetaInfo metaInfo = positionsAndSizesByNames.get(fileName);

        if (metaInfo == null || !metaInfo.isPresent()) {
            return;
        }

        synchronized (updateMapLock) {
            positionsAndSizesByNames.remove(fileName);
        }
    }

    Map<String, MetaInfo> getMap() {
        return new HashMap<>(positionsAndSizesByNames);
    }
}
