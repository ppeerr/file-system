package per.demo.extendable;

import per.demo.InFileFileStoreView;
import per.demo.model.FileInfo;
import per.demo.model.MetaInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

public class InFileFileStoreViewImpl implements InFileFileStoreView {

    private ConcurrentMap<String, MetaInfo> positionsAndSizesByNames = new ConcurrentHashMap<>();
    private final Object updateMapLock = new Object();

    @Override
    public boolean contains(String fileName) {
        return positionsAndSizesByNames.containsKey(fileName);
    }

    @Override
    public MetaInfo getMeta(String fileName) {
        MetaInfo metaInfo = positionsAndSizesByNames.get(fileName);

        return metaInfo != null
                ? new MetaInfo(metaInfo)
                : null;
    }

    @Override
    public void putMeta(List<FileInfo> fileInfos) {
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

    @Override
    public void remove(String fileName) {
        MetaInfo metaInfo = positionsAndSizesByNames.get(fileName);

        if (isMetaInfoDoesNotExist(metaInfo)) {
            return;
        }

        synchronized (updateMapLock) {
            positionsAndSizesByNames.remove(fileName);
        }
    }

    @Override
    public Map<String, MetaInfo> getMap() {
        return new HashMap<>(positionsAndSizesByNames);
    }

    private boolean isMetaInfoDoesNotExist(MetaInfo metaInfo) {
        return metaInfo == null || !metaInfo.isPresent();
    }
}
